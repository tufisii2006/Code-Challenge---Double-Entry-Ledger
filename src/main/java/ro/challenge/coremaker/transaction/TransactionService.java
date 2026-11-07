package ro.challenge.coremaker.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.challenge.coremaker.account.AccountEntity;
import ro.challenge.coremaker.account.AccountRepository;
import ro.challenge.coremaker.dto.CreateTransactionRequest;
import ro.challenge.coremaker.dto.Entry;
import ro.challenge.coremaker.dto.EntryRequest;
import ro.challenge.coremaker.dto.Transaction;
import ro.challenge.coremaker.entry.EntryEntity;
import ro.challenge.coremaker.events.TransactionRecordedEvent;
import ro.challenge.coremaker.exceptions.LedgerRuleViolation;
import ro.challenge.coremaker.exceptions.TransactionAlreadyReversedException;
import ro.challenge.coremaker.exceptions.TransactionNotFoundException;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {


    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    private final ApplicationEventPublisher eventPublisher;
    private final TransactionValidator transactionValidator;

    @Transactional
    public Transaction createTransaction(String idempotencyKey, CreateTransactionRequest request) {
        log.info("Creating transaction: {}", request);

        // Idempotency: if this key was already used, return the transaction it created
        Optional<TransactionEntity> existingTransactionOptional = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTransactionOptional.isPresent()) {
            var existingTransaction = existingTransactionOptional.get();
            return new Transaction().id(UUID.fromString(existingTransaction.getId()))
                .entries(existingTransaction.getEntries().stream().map(ent ->
                        new Entry().accountId(UUID.fromString(ent.getAccount().getId()))
                            .amount(String.valueOf(ent.getAmount()))
                            .direction(Entry.DirectionEnum.fromValue(ent.getDirection().toString()))
                    )
                    .toList())
                .recordedAt(existingTransaction.getRecordedAt().toString())
                .description(existingTransaction.getDescription());
        }

        Map<String, AccountEntity> accounts = accountRepository.findAllById(
                request.getEntries().stream()
                    .map(e -> e.getAccountId().toString())
                    .collect(Collectors.toSet())).stream()
            .collect(Collectors.toMap(AccountEntity::getId, a -> a));

        transactionValidator.validate(accounts, request);

        List<EntryEntity> entries = new ArrayList<>();
        for (EntryRequest entry : request.getEntries()) {
            AccountEntity account = accounts.get(entry.getAccountId().toString());
            EntryEntity.Direction direction = EntryEntity.Direction.valueOf(entry.getDirection().getValue());
            long amount = toMinorUnits(entry.getAmount(), account.getCurrency());

            if (direction == EntryEntity.Direction.CREDIT) {
                account.credit(amount);
            } else {
                account.debit(amount);
            }

            entries.add(EntryEntity.builder()
                .account(account)
                .direction(direction)
                .amount(amount)
                .build());
        }

        var transaction = TransactionEntity.builder()
            .description(request.getDescription())
            .idempotencyKey(idempotencyKey)
            .entries(entries)
            .build();

        var transactionResponse = transactionRepository.save(transaction);

        log.info("Transaction created: {}. Publish-ing it to Kafka for other consumers.", transactionResponse);
        eventPublisher.publishEvent(TransactionRecordedEvent.from(transactionResponse));

        return new Transaction().id(UUID.fromString(transactionResponse.getId()))
            .entries(transaction.getEntries().stream().map(ent ->
                    new Entry().accountId(UUID.fromString(ent.getAccount().getId()))
                        .amount(String.valueOf(ent.getAmount()))
                        .direction(Entry.DirectionEnum.fromValue(ent.getDirection().toString()))
                )
                .toList())
            .recordedAt(transaction.getRecordedAt().toString())
            .description(transactionResponse.getDescription());
    }


    @Transactional
    public Transaction reverseTransaction(String originalId, String idempotencyKey) {
        // Idempotency: if this key was already used, return the transaction it created
        Optional<TransactionEntity> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            log.info("Returning the same transaction entity: {}", existing.get());
            return new Transaction().id(UUID.fromString(existing.get().getId()))
                .entries(existing.get().getEntries().stream().map(ent ->
                        new Entry().accountId(UUID.fromString(ent.getAccount().getId()))
                            .amount(String.valueOf(ent.getAmount()))
                            .direction(Entry.DirectionEnum.fromValue(ent.getDirection().toString()))
                    )
                    .toList())
                .recordedAt(existing.get().getRecordedAt().toString())
                .description(existing.get().getDescription());
        }

        TransactionEntity original = transactionRepository.findById(originalId)
            .orElseThrow(() -> new TransactionNotFoundException(originalId));

        // A reversal can't be reversed
        if (original.getReverses() != null) {
            throw new LedgerRuleViolation("A reversal can't be reversed");
        }

        // A transaction can be reversed only once
        if (transactionRepository.findByReversesId(originalId).isPresent()) {
            throw new TransactionAlreadyReversedException("The transaction was already reverted. TransactionID: " + originalId);
        }

        // Lock the accounts in id order, same as for a normal transaction
        Set<String> accountIds = original.getEntries().stream()
            .map(e -> e.getAccount().getId())
            .collect(Collectors.toSet());
        Map<String, AccountEntity> accounts = accountRepository.findAllForUpdate(accountIds).stream()
            .collect(Collectors.toMap(AccountEntity::getId, a -> a));

        // Overdraft check on the net change per account, with signs reversed:
        // an original DEBIT becomes a credit, so it increases the balance.
        Map<String, Long> deltas = new HashMap<>();
        for (EntryEntity e : original.getEntries()) {
            long signed = e.getDirection() == EntryEntity.Direction.DEBIT ? e.getAmount() : -e.getAmount();
            deltas.merge(e.getAccount().getId(), signed, Long::sum);
        }
        for (Map.Entry<String, Long> delta : deltas.entrySet()) {
            AccountEntity account = accounts.get(delta.getKey());
            if (!account.allowsBalance(account.getBalance() + delta.getValue())) {
                throw new LedgerRuleViolation("Reversal would take account " + account.getId() + " past its overdraft limit");
            }
        }

        // Apply the flipped entries and store the balance after each one
        List<EntryEntity> entries = new ArrayList<>();
        for (EntryEntity e : original.getEntries()) {
            AccountEntity account = accounts.get(e.getAccount().getId());
            EntryEntity.Direction flipped = e.getDirection() == EntryEntity.Direction.DEBIT
                ? EntryEntity.Direction.CREDIT
                : EntryEntity.Direction.DEBIT;

            if (flipped == EntryEntity.Direction.CREDIT) {
                account.credit(e.getAmount());
            } else {
                account.debit(e.getAmount());
            }

            entries.add(EntryEntity.builder()
                .account(account)
                .direction(flipped)
                .amount(e.getAmount())
                .build());
        }

        TransactionEntity reversal = TransactionEntity.builder()
            .description("Reversal of " + originalId)
            .reverses(original)
            .idempotencyKey(idempotencyKey)
            .entries(entries)
            .build();

        TransactionEntity saved = transactionRepository.save(reversal);
        log.info("Sending transaction reversal to Kafka: {}", saved);
        eventPublisher.publishEvent(TransactionRecordedEvent.from(saved));
        return new Transaction().description("Reversal of " + originalId)
            .reversesId(UUID.fromString(original.getId()))
            .id(UUID.fromString(reversal.getId()));
    }

    // "100.50" in EUR -> 10050. Java's Currency already knows the decimal places (EUR 2, JPY 0).
    private static long toMinorUnits(String amount, String currencyCode) {
        int decimals = Currency.getInstance(currencyCode).getDefaultFractionDigits();
        return new BigDecimal(amount).movePointRight(decimals).longValueExact();
    }
}
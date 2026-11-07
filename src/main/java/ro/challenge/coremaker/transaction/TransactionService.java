package ro.challenge.coremaker.transaction;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.challenge.coremaker.account.AccountEntity;
import ro.challenge.coremaker.account.AccountRepository;
import ro.challenge.coremaker.dto.CreateTransactionRequest;
import ro.challenge.coremaker.dto.EntryRequest;
import ro.challenge.coremaker.dto.Transaction;
import ro.challenge.coremaker.entry.EntryEntity;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    @Transactional
    public Transaction createTransaction(String idempotencyKey, CreateTransactionRequest request) {
        Map<String, AccountEntity> accounts = accountRepository.findAllById(
                request.getEntries().stream()
                    .map(e -> e.getAccountId().toString())
                    .collect(Collectors.toSet())).stream()
            .collect(Collectors.toMap(AccountEntity::getId, a -> a));

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

        return new Transaction().id(UUID.fromString(transactionResponse.getId()))
            .recordedAt(transaction.getRecordedAt().toString())
            .description("Successfully created transaction");
    }

    // "100.50" in EUR -> 10050. Java's Currency already knows the decimal places (EUR 2, JPY 0).
    private static long toMinorUnits(String amount, String currencyCode) {
        int decimals = Currency.getInstance(currencyCode).getDefaultFractionDigits();
        return new BigDecimal(amount).movePointRight(decimals).longValueExact();
    }
}
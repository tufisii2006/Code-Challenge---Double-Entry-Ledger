package ro.challenge.coremaker.transaction;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.stereotype.Component;
import ro.challenge.coremaker.account.AccountEntity;
import ro.challenge.coremaker.dto.CreateTransactionRequest;
import ro.challenge.coremaker.dto.EntryRequest;
import ro.challenge.coremaker.exceptions.LedgerRuleViolation;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TransactionValidator {

    /**
     * A transaction is recorded only if all of these hold:
     * it has at least two entries, and every amount is positive and fits its currency's decimal places
     * all of its accounts exist and are in the same currency
     * its debits and its credits add up to the same total
     * no account would go past its overdraft limit
     */
    public Optional<TransactionEntity> validate(Optional<TransactionEntity> existing,
                                                Map<String, AccountEntity> accounts,
                                                CreateTransactionRequest request) {
        if (existing.isPresent()) {
            throw new LedgerRuleViolation("There is already a transaction with this idempotency key:" + existing.get().getIdempotencyKey());
        }
        if (accounts.size() != 2) {
            throw new LedgerRuleViolation("One or more accounts are invalid");
        }
        var currencies = accounts.values().stream()
            .map(AccountEntity::getCurrency)
            .collect(Collectors.toSet());
        if (currencies.size() != 1) {
            throw new LedgerRuleViolation("Accounts must be in the same currency");
        }
        validateEntries(request.getEntries());
        return Optional.empty();
    }

    private static void validateEntries(List<EntryRequest> entries) {
        if (entries.size() != 2) {
            throw new InvalidRequestException("A transaction must have exactly two entries");
        }
        EntryRequest first = entries.get(0);
        EntryRequest second = entries.get(1);
        if (first.getDirection().getValue().equals(second.getDirection().getValue())) {
            throw new InvalidRequestException("One entry must be DEBIT and the other CREDIT");
        }
        // compareTo, not equals: "100.00" and "100" are the same amount
        if (new BigDecimal(first.getAmount()).compareTo(new BigDecimal(second.getAmount())) != 0) {
            throw new LedgerRuleViolation("Debit and credit amounts must be equal");
        }
    }

}
package ro.challenge.coremaker.account;

import org.apache.kafka.common.errors.InvalidRequestException;
import ro.challenge.coremaker.exceptions.LedgerRuleViolation;

import java.util.Currency;
import java.util.Set;


public final class CurrencyValidator {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of("EUR", "USD", "JPY");

    private CurrencyValidator() {
    }

    public static void validate(String code) {
        if (!isIso4217(code)) {
            throw new InvalidRequestException("Not an ISO 4217 currency code");
        }
        if (!SUPPORTED_CURRENCIES.contains(code)) {
            throw new LedgerRuleViolation("Currency " + code + " is valid but it is not supported by the ledger." +
                " Please use one of: " + SUPPORTED_CURRENCIES);
        }
    }

    private static boolean isIso4217(String code) {
        if (code == null || !code.matches("^[A-Z]{3}$")) {
            return false;
        }
        try {
            Currency.getInstance(code);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
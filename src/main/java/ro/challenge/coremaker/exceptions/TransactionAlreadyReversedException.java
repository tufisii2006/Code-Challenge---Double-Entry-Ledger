package ro.challenge.coremaker.exceptions;

public class TransactionAlreadyReversedException extends RuntimeException {
    public TransactionAlreadyReversedException(String message) {
        super(message);
    }
}

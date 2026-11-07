package ro.challenge.coremaker.events;

import ro.challenge.coremaker.transaction.TransactionEntity;

import java.time.Instant;
import java.util.List;

public record TransactionRecordedEvent(
    String transactionId,
    String description,
    String reversesId,
    Instant recordedAt,
    List<EntryEvent> entries) {

    public record EntryEvent(String accountId, String currency, String direction, long amount) {
    }

    public static TransactionRecordedEvent from(TransactionEntity transaction) {
        List<EntryEvent> entries = transaction.getEntries().stream()
            .map(e -> new EntryEvent(
                e.getAccount().getId(),
                e.getAccount().getCurrency(),
                e.getDirection().name(),
                e.getAmount()))
            .toList();

        return new TransactionRecordedEvent(
            transaction.getId(),
            transaction.getDescription(),
            transaction.getReverses() == null ? null : transaction.getReverses().getId(),
            transaction.getRecordedAt(),
            entries);
    }
}

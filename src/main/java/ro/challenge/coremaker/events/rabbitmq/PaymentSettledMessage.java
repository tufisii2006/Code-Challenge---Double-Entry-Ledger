package ro.challenge.coremaker.events.rabbitmq;

public record PaymentSettledMessage(
    String eventId,
    String paymentId,
    String merchantAccountId,
    String amount,
    String currency) {
}

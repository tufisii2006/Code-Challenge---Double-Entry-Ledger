package ro.challenge.coremaker.events.rabbitmq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ro.challenge.coremaker.exceptions.LedgerRuleViolation;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSettledListener {

    private final SettlementService settlementService;

    @RabbitListener(queues = RabbitConfig.PAYMENTS_SETTLED_QUEUE)
    public void onPaymentSettled(PaymentSettledMessage message) {
        log.info("Payment settled received: eventId={} paymentId={}", message.eventId(), message.paymentId());
        try {
            settlementService.settle(message);
        } catch (LedgerRuleViolation | InvalidRequestException | IllegalArgumentException e) {
            // Wrong data: don't retry, just send it to the dead-letter queue
            log.warn("Rejecting payment {}: {}", message.paymentId(), e.getMessage());
            throw new AmqpRejectAndDontRequeueException(e.getMessage(), e);
        }
    }
}
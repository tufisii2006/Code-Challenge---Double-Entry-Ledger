package ro.challenge.coremaker.events;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import static ro.challenge.coremaker.events.KafkaConfig.TRANSACTIONS_RECORDED;


@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // Runs only after the database commit, so rolled-back transactions are never announced
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publish(TransactionRecordedEvent event) {
        kafkaTemplate.send(TRANSACTIONS_RECORDED, event.transactionId(), event);
    }
}
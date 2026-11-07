package ro.challenge.coremaker.events;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TRANSACTIONS_RECORDED = "ledger.transactions.recorded";

    @Bean
    NewTopic transactionsRecordedTopic() {
        return TopicBuilder.name(TRANSACTIONS_RECORDED)
            .partitions(3)
            .replicas(1)
            .build();
    }
}
package ro.challenge.coremaker.events.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    public static final String PAYMENTS_EXCHANGE = "payments";
    public static final String PAYMENT_SETTLED_ROUTING_KEY = "payment.settled";
    public static final String PAYMENTS_SETTLED_QUEUE = "payments.settled";

    public static final String DEAD_LETTER_EXCHANGE = "payments.dlx";
    public static final String DEAD_LETTER_QUEUE = "payments.settled.dlq";

    @Bean
    DirectExchange paymentsExchange() {
        return new DirectExchange(PAYMENTS_EXCHANGE);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    Queue paymentsSettledQueue() {
        return QueueBuilder.durable(PAYMENTS_SETTLED_QUEUE)
            .withArgument("x-dead-letter-exchange", DEAD_LETTER_EXCHANGE)
            .withArgument("x-dead-letter-routing-key", DEAD_LETTER_QUEUE)
            .build();
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    Binding paymentsSettledBinding() {
        return BindingBuilder.bind(paymentsSettledQueue())
            .to(paymentsExchange())
            .with(PAYMENT_SETTLED_ROUTING_KEY);
    }

    @Bean
    Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
            .to(deadLetterExchange())
            .with(DEAD_LETTER_QUEUE);
    }

    @Bean
    MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
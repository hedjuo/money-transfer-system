package com.acme.bank.management.application.config;

import com.acme.bank.payments.infrastructure.message.TransactionEventMessage;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.consumer.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${app.kafka.topic.transaction-events:transaction-events}")
    private String kafkaTopic;

    @Bean
    public ReceiverOptions<String, TransactionEventMessage> receiverOptions() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "accounts-updater");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, TransactionEventMessage.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

        return ReceiverOptions.<String, TransactionEventMessage>create(props)
                .subscription(Collections.singleton(kafkaTopic));
    }

    @Bean
    public KafkaReceiver<String, TransactionEventMessage> kafkaReceiver(ReceiverOptions<String, TransactionEventMessage> receiverOptions) {
        return KafkaReceiver.create(receiverOptions);
    }
}

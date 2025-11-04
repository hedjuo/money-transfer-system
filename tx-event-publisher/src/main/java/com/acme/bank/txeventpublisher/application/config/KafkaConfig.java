package com.acme.bank.txeventpublisher.application.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import org.springframework.kafka.core.*;
import org.springframework.kafka.transaction.KafkaTransactionManager;

@Configuration
public class KafkaConfig {
    @Value("${kafka.bootstrap-servers}")
    private String bootstrap;

    @Value("${kafka.client-id}")
    private String clientId;

    @Value("${kafka.transactional-id-prefix}")
    private String txPrefix;

    @Value("${app.instance-id}")
    private String instanceId;

    @Value("${kafka.acks}")
    private String acks;

    @Value("${kafka.retries}")
    private Integer retries;

    @Value("${kafka.request-timeout-ms}")
    private Integer requestTimeoutMs;

    @Value("${kafka.delivery-timeout-ms}")
    private Integer deliveryTimeoutMs;

    @Value("${kafka.enable-idempotence}")
    private Boolean enableIdempotence;

    @Value("${kafka.linger-ms}")
    private Integer lingerMs;

    @Value("${kafka.compression-type}")
    private String compressionType;

    @Bean
    public ProducerFactory<String, String> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, clientId + "-" + instanceId);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, acks);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, enableIdempotence);
        props.put(ProducerConfig.RETRIES_CONFIG, retries);
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, requestTimeoutMs);
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, deliveryTimeoutMs);
        props.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, compressionType);
        // Important: stable transactional.id (one per instance)
        props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, txPrefix + instanceId);
        // Recommended defaults for EOS v2 (Kafka 2.5+)
        props.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory());
        template.setObservationEnabled(true);
        return template;
    }

    @Bean
    public KafkaTransactionManager<String, String> kafkaTxManager(ProducerFactory<String, String> pf) {
        return new KafkaTransactionManager<>(pf);
    }
}

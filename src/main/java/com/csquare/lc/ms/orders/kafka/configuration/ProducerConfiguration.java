package com.csquare.lc.ms.orders.kafka.configuration;

import com.csquare.ms.lib.topics.MsApiTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class ProducerConfiguration {

    @Value("${kafka.common.BOOTSTRAP_SERVERS_CONFIG}")
    private String BOOTSTRAP_SERVERS_CONFIG;

    @Value("${kafka.producer.CLIENT_ID_CONFIG}")
    private String CLIENT_ID_CONFIG;

    @Value("${kafka.producer.BATCH_SIZE_CONFIG}")
    private String BATCH_SIZE_CONFIG;

    @Value("${kafka.producer.LINGER_MS_CONFIG}")
    private String LINGER_MS_CONFIG;

    @Value("${kafka.producer.COMPRESSION_TYPE_CONFIG}")
    private String COMPRESSION_TYPE_CONFIG;

    @Value("${kafka.producer.ACKS_CONFIG:all}")
    private String ACKS_CONFIG;

    @Value("${kafka.producer.RETRIES_CONFIG:2147483647}")
    private String RETRIES_CONFIG;

    @Value("${kafka.producer.ENABLE_IDEMPOTENCE_CONFIG:true}")
    private String ENABLE_IDEMPOTENCE_CONFIG;

    @Value("${kafka.producer.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION:1}")
    private String MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION;

    @Bean("producerProperties")
    public Map<String, Object> producerProperties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, CLIENT_ID_CONFIG);
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS_CONFIG);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        //Safe Producer
        properties.put(ProducerConfig.ACKS_CONFIG, ACKS_CONFIG);
        properties.put(ProducerConfig.RETRIES_CONFIG, RETRIES_CONFIG);
        properties.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, ENABLE_IDEMPOTENCE_CONFIG);

        //High Throughput
        properties.put(ProducerConfig.LINGER_MS_CONFIG, LINGER_MS_CONFIG);
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, BATCH_SIZE_CONFIG);
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, COMPRESSION_TYPE_CONFIG);

        // Ordering
        properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION);

        return properties;
    }

    @Bean
    public KafkaTemplate<String, MsApiTopic> kafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProperties()));
    }

}
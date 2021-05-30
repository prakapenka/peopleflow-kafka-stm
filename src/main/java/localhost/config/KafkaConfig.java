package localhost.config;


import localhost.data.Employee;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
@Setter
@ConfigurationProperties("spring.kafka.producer")
public class KafkaConfig {

    private String bootstrapServers;

    @Bean
    public KafkaSender<?, Employee> getKafkaSender() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaSerializer.class);

        SenderOptions<Integer, Employee> senderOptions =
                SenderOptions.<Integer, Employee>create(producerProps)
                        .maxInFlight(1024);

        return KafkaSender.create(senderOptions);
    }
}

package localhost.config;


import localhost.data.States;
import localhost.data.kafka.EmployeeEvent;
import localhost.data.kafka.EmployeeEventSerializer;
import localhost.data.kafka.StatesSerializer;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.kafka.clients.producer.ProducerConfig;
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
    public KafkaSender<States, EmployeeEvent> getKafkaSender() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StatesSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EmployeeEventSerializer.class);

        SenderOptions<States, EmployeeEvent> senderOptions =
                SenderOptions.<States, EmployeeEvent>create(producerProps)
                        .maxInFlight(1024);

        return KafkaSender.create(senderOptions);
    }
}

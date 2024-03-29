package localhost.config;


import java.util.HashMap;
import java.util.Map;
import localhost.data.States;
import localhost.data.kafka.EmployeeEvent;
import localhost.data.kafka.EmployeeEventSerializer;
import localhost.data.kafka.StatesSerializer;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

@RequiredArgsConstructor
@Configuration
@Setter
public class KafkaSenderConfig {

    private final KafkaConfig config;

    @Bean
    public KafkaSender<States, EmployeeEvent> getKafkaSender() {
        Map<String, Object> producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StatesSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, EmployeeEventSerializer.class);

        SenderOptions<States, EmployeeEvent> senderOptions =
                SenderOptions.<States, EmployeeEvent>create(producerProps)
                        .maxInFlight(1024);

        return KafkaSender.create(senderOptions);
    }
}

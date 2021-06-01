package localhost.config;

import localhost.data.States;
import localhost.data.kafka.EmployeeDeserializer;
import localhost.data.kafka.EmployeeEvent;
import localhost.data.kafka.StatesDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Configuration;
import reactor.core.Disposable;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KafkaListenerConfig {

    private final KafkaConfig config;

    private final Function<ReceiverRecord<States, EmployeeEvent>, EmployeeEvent> listener;

    private Disposable disposable;

    @PostConstruct
    public void configureListenerStream() {
        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, config.getGroup());
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StatesDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EmployeeDeserializer.class);

        ReceiverOptions<States, EmployeeEvent> receiverOptions =
                ReceiverOptions.<States, EmployeeEvent>create(consumerProps)
                        .subscription(Collections.singleton(config.getTopic()));

        var flux = KafkaReceiver.create(receiverOptions).receive();

        this.disposable = flux.map(listener).subscribe(
                event -> log.info("Event processed: " + event),
                error -> log.error("Error to process event: ", error)
        );
    }

    @PreDestroy
    void preDestroy() {
        this.disposable.dispose();
    }


}

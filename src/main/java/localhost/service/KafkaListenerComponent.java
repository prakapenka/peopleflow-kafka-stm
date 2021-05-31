package localhost.service;

import localhost.data.States;
import localhost.data.kafka.EmployeeDeserializer;
import localhost.data.kafka.EmployeeEvent;
import localhost.data.kafka.StatesDeserializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Component
public class KafkaListenerComponent {

    private final KafkaEventsProcessor function;

    private Disposable d;

    @PostConstruct
    void init() {

        Map<String, Object> consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "sample-group");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StatesDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, EmployeeDeserializer.class);

        ReceiverOptions<States, EmployeeEvent> receiverOptions =
                ReceiverOptions.<States, EmployeeEvent>create(consumerProps)
                        .subscription(Collections.singleton("test"));

        Flux<ReceiverRecord<States, EmployeeEvent>> inboundFlux =
                KafkaReceiver.create(receiverOptions)
                        .receive();

        d = inboundFlux.map(function).subscribe();
    }

    @PreDestroy
    void destruct() {
        d.dispose();
    }


}

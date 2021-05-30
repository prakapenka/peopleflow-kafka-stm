package localhost.service;

import localhost.data.Employee;
import localhost.data.States;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.function.Function;

@Slf4j
@Component
public class KafkaEventsProcessor implements Function<ReceiverRecord<States, Employee>, Employee> {

    @Override
    public Employee apply(ReceiverRecord<States, Employee> stringEmployeeReceiverRecord) {
        log.info("gotcha");
        var key = stringEmployeeReceiverRecord.key();
        var emp = stringEmployeeReceiverRecord.value();


        return null;
    }
}

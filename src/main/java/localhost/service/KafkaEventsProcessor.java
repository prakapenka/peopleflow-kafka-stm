package localhost.service;

import localhost.data.Event;
import localhost.data.States;
import localhost.data.kafka.EmployeeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.function.Function;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventsProcessor implements Function<ReceiverRecord<States, EmployeeEvent>, EmployeeEvent> {

    private final StateMachine<States, Event> stateMachine;

    private StateMachinePersister<States, Event, String> stateMachinePersist;

    @Autowired
    public void setStateMachinePersist(StateMachinePersister<States, Event, String> stateMachinePersist) {
        this.stateMachinePersist = stateMachinePersist;
    }

    @Override
    public EmployeeEvent apply(ReceiverRecord<States, EmployeeEvent> record) {

        var value = record.value();
        var employee = value.getEmployee();
        var event = value.getEvent();
        var email = employee.getEmail();

        try {
            var stateMachine = resetStateMachineFromStore(email);
            stateMachine.sendEvent(Mono.just(
                    MessageBuilder.withPayload(event).build()
            )).blockLast();

            stateMachinePersist.persist(stateMachine, email);

        } catch (Exception e) {
            log.error("Can't process event", e);
        }

        return value;
    }

    private StateMachine<States, Event> resetStateMachineFromStore(String email) throws Exception {
        return stateMachinePersist.restore(stateMachine, email);
    }
}

package localhost.service;

import java.util.function.Function;
import localhost.data.Event;
import localhost.data.States;
import localhost.data.kafka.EmployeeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.persist.StateMachinePersister;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

/**
 * This is simplified example of kafka events listener.
 *
 * For every incoming message from kafka, local stateMachine got re-initialized with
 * context taken from local memory store.
 *
 * Using one same instance of state machine gives ability to reuse same
 * state machine for different events (different employees).
 *
 * Due to current configuration, this bean of type 'KafkaEventsProcessor' and as far instance of
 * stateMachine both are singletons.
 *
 * Instantiation of new stateMachine per-call could be expensive operation. Compare to switching
 * context for same state-machine instance.
 *
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventsProcessor implements Function<ReceiverRecord<States, EmployeeEvent>, EmployeeEvent> {

    private final StateMachine<States, Event> stateMachine;

    private final StateMachinePersister<States, Event, String> stateMachinePersist;

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

package localhost.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import localhost.config.StateMachineConfig;
import localhost.data.Employee;
import localhost.data.Event;
import localhost.data.States;
import localhost.data.kafka.EmployeeEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.kafka.receiver.ReceiverRecord;

@ExtendWith({SpringExtension.class, MockitoExtension.class})
@ContextConfiguration(classes = {StateMachineConfig.class, KafkaEventsProcessor.class})
public class KafkaEventsProcessorTest {

    private static final String EMAIL_1 = "john.smith@example.com";

    @Autowired
    private KafkaEventsProcessor processor;

    @Autowired
    private StateMachineConfig.InMemoryStateMachinePersist contextStore;

    @Mock
    private ReceiverRecord<States, EmployeeEvent> record;

    @BeforeEach
    void beforeEach() {
        assertEquals(0, contextStore.getInternalStates().count());
    }

    @Test
    @DirtiesContext
    void testCanMoveToAddState() {
        EmployeeEvent event = getEvent(Event.CREATE);
        when(record.value()).thenReturn(event);
        processor.apply(record);

        var stateInfo = contextStore.getStateForEmail(EMAIL_1);
        assertTrue(stateInfo.isPresent());
        assertEquals(States.ADDED, stateInfo.get().getState());
    }

    @Test
    @DirtiesContext
    void testSkipWrongCheckState() {
        EmployeeEvent event = getEvent(Event.CHECK);
        when(record.value()).thenReturn(event);
        processor.apply(record);

        var stateInfo = contextStore.getStateForEmail(EMAIL_1);
        assertTrue(stateInfo.isPresent(),
                "For wrong state we do track state machines with NOT_EXISTED states");
        assertEquals(States.NOT_EXISTED, stateInfo.get().getState());
    }

    @Test
    @DirtiesContext
    void testSkipWrongApproveState() {
        EmployeeEvent event = getEvent(Event.APPROVE);
        when(record.value()).thenReturn(event);
        processor.apply(record);

        var stateInfo = contextStore.getStateForEmail(EMAIL_1);
        assertTrue(stateInfo.isPresent(),
                "For wrong state we do track state machines with NOT_EXISTED states");
        assertEquals(States.NOT_EXISTED, stateInfo.get().getState());
    }

    /**
     * Test what will happens if there is more then one employee with different states
     */
    @Test
    @DirtiesContext
    void testMultipleModifications() {
        // send create event for first email
        EmployeeEvent event1 = getEvent(EMAIL_1, Event.CREATE);
        when(record.value()).thenReturn(event1);
        processor.apply(record);

        // send create event for second email
        String EMAIL_2 = "smith.john@example.com";
        EmployeeEvent event2 = getEvent(EMAIL_2, Event.CREATE);
        when(record.value()).thenReturn(event2);
        processor.apply(record);

        // send check event for third email
        event1 = getEvent(EMAIL_1, Event.CHECK);
        when(record.value()).thenReturn(event1);
        processor.apply(record);

        assertEquals(2, contextStore.getInternalStates().count());

        var stateInfo = contextStore.getStateForEmail(EMAIL_1);
        assertTrue(stateInfo.isPresent(), "First email sends proper event 'CREATE'");
        assertEquals(States.IN_CHECK, stateInfo.get().getState());

        stateInfo = contextStore.getStateForEmail(EMAIL_2);
        assertTrue(stateInfo.isPresent(), "second email sends wrong email 'APPROVE'");
        assertEquals(States.ADDED, stateInfo.get().getState());
    }

    private EmployeeEvent getEvent(Event event) {
        return this.getEvent(EMAIL_1, event);
    }

    private EmployeeEvent getEvent(String email, Event event) {
        Employee employee = mock(Employee.class);
        when(employee.getEmail()).thenReturn(email);
        return new EmployeeEvent().setEmployee(employee).setEvent(event);
    }
}

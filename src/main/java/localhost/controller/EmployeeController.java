package localhost.controller;

import localhost.config.StateMachineConfig;
import localhost.data.Employee;
import localhost.data.Event;
import localhost.data.StateInfo;
import localhost.data.States;
import localhost.data.kafka.EmployeeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EmployeeController {

    private final KafkaSender<States, EmployeeEvent> sender;

    private final StateMachineConfig.InMemoryStateMachinePersist stateManager;

    @PutMapping("create")
    public Flux<Employee> createEmployee(@RequestBody final Employee employee) {
        var event = new EmployeeEvent().setEvent(Event.CREATED).setEmployee(employee);

        return sender.send(
                Mono.just(event)
                        .map(e -> SenderRecord.create("test", null, null,
                                null, e, null))
        ).map(r -> employee);
    }

    @PostMapping("in-check")
    public Flux<String> setInCheckEmployee(@RequestBody String email) {
        var event = new EmployeeEvent().setEvent(Event.CHECK)
                .setEmployee(new Employee().setEmail(email));

        return sender.send(
                Mono.just(event)
                        .map(e -> SenderRecord.create("test", null, null,
                                States.IN_CHECK,
                                e, null))
        ).map(r -> email);
    }

    @GetMapping
    public Flux<StateInfo> getEmployeesInfo() {
        return Flux.fromStream(stateManager.getInternalStates());
    }
}

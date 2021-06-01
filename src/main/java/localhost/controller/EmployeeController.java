package localhost.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import localhost.config.StateMachineConfig;
import localhost.data.Employee;
import localhost.data.Event;
import localhost.data.StateInfo;
import localhost.data.States;
import localhost.data.kafka.EmployeeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.text.MessageFormat;
import java.util.function.Function;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
public class EmployeeController {

    private final KafkaSender<States, EmployeeEvent> sender;

    private final StateMachineConfig.InMemoryStateMachinePersist stateManager;

    private final Function<SenderResult<?>, String> stringFunction =
            r -> MessageFormat.format("topic: {0}, partition: {1}, offset: {2}",
                    r.recordMetadata().topic(),
                    r.recordMetadata().partition(),
                    r.recordMetadata().offset()
            );

    @ApiOperation(value = "Create new employee")
    @PutMapping("create")
    public Flux<String> createEmployee(@RequestBody @Valid final Employee employee) {
        var event = new EmployeeEvent().setEvent(Event.CREATE)
                .setEmployee(employee);
        return sender.send(
                Mono.just(event)
                        .map(e -> SenderRecord.create("test", null, null,
                                null, e, null))
        ).map(stringFunction);
    }

    @ApiOperation(value = "Update employee status by email")
    @PostMapping("update")
    public Flux<String> sendEmployeeEvent(
            @ApiParam(value = "Unique employee email", required = true,
                    example = "john.smith@example.com")
            @RequestParam @Email @Valid String email,
            @ApiParam(value = "Event type", required = true, example = "CHECK",
                    allowableValues = "CHECK,APPROVE,ACTIVATE")
            @RequestParam @NotNull Event event) {

        var employeeEvent = new EmployeeEvent().setEvent(event)
                .setEmployee(new Employee().setEmail(email));

        return sender.send(
                Mono.just(employeeEvent)
                        .map(e -> SenderRecord.create("test", null, null,
                                States.IN_CHECK,
                                e, null))
        ).map(stringFunction);
    }

    @ApiOperation(value = "Get information about employees states")
    @GetMapping
    public Flux<StateInfo> getEmployeesInfo() {
        return Flux.fromStream(stateManager.getInternalStates());
    }
}

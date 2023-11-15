package localhost.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import java.text.MessageFormat;
import java.util.function.Function;
import localhost.config.StateMachineConfig;
import localhost.data.Employee;
import localhost.data.Event;
import localhost.data.StateInfo;
import localhost.data.States;
import localhost.data.kafka.EmployeeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

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

    @Operation(summary = "Create new employee")
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

    @Operation(summary = "Update employee status by email")
    @PostMapping("update")
    public Flux<String> sendEmployeeEvent(
            @Parameter(description = "Unique employee email", required = true,
                    example = "john.smith@example.com")
            @RequestParam @Email @Valid String email,
            @Parameter(description = "Event type: CHECK,APPROVE,ACTIVATE", required = true, example = "CHECK")
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

    @Operation(summary = "Get information about employees states")
    @GetMapping
    public Flux<StateInfo> getEmployeesInfo() {
        return Flux.fromStream(stateManager.getInternalStates());
    }
}

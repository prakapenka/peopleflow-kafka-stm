package localhost.controller;

import localhost.data.Employee;
import localhost.data.States;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EmployeeController {

    private final KafkaSender<States, Employee> sender;


    @PutMapping("create")
    public Flux<Employee> createEmployee(@RequestBody final Employee employee) {
        return sender.send(
                Mono.just(employee)
                        .map(e -> SenderRecord.create("test", null, null,
                                States.ADDED,
                                employee, null))
        ).map(r -> employee);
    }

    @PostMapping("in-check")
    public Flux<Employee> setInCheckEmployee(@RequestBody Employee employee) {
        return sender.send(
                Mono.just(employee)
                        .map(e -> SenderRecord.create("test", null, null,
                                States.IN_CHECK,
                                employee, null))
        ).map(r -> employee);
    }

}

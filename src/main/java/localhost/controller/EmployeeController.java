package localhost.controller;

import localhost.data.Employee;
import localhost.data.States;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

@Slf4j
@RestController
@RequiredArgsConstructor
public class EmployeeController {

    private final KafkaSender<States, Employee> sender;

    @PutMapping("create")
    public Flux<Employee> createEmployee(@RequestBody Employee employee) {
        log.info("get Employee");
        return sender.send(Mono.just(employee).map(
                e -> SenderRecord.create("test", null, null, States.ADDED,
                        employee, null)))
                .map(r -> {
                    var m = r.recordMetadata();
                    var topic = m.topic();
                    return employee;
                });
    }
}

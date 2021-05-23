package localhost.controller;

import localhost.data.Employee;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@RestController
public class EmployeeController {

    @GetMapping("api")
    public Mono<Employee> getEmployee() {
        log.info("get Employee");
        return Mono.just(new Employee().setAge(1).setName("name").setContractInfo("info"));
    }
}

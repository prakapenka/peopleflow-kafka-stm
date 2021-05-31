package localhost.data.kafka;

import localhost.data.Employee;
import localhost.data.Event;
import lombok.Data;

@Data
public class EmployeeEvent {

    private Event event;

    private Employee employee;
}

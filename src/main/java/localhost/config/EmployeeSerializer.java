package localhost.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import localhost.data.Employee;
import org.apache.kafka.common.serialization.Serializer;

public class EmployeeSerializer implements Serializer<Employee> {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public byte[] serialize(String s, Employee employee) {
        try {
            return om.writeValueAsBytes(employee);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

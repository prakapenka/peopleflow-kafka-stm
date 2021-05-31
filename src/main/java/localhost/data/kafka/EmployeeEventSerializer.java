package localhost.data.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;

public class EmployeeEventSerializer implements Serializer<EmployeeEvent> {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    public byte[] serialize(String s, EmployeeEvent employee) {
        try {
            return om.writeValueAsBytes(employee);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

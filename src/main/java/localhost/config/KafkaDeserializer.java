package localhost.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import localhost.data.Employee;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class KafkaDeserializer implements Deserializer<Employee> {

    private final ObjectMapper om = new ObjectMapper();
    Logger LOG = LoggerFactory.getLogger(KafkaDeserializer.class);

    @Override
    public Employee deserialize(String s, byte[] bytes) {
        try {
            return om.readValue(bytes, Employee.class);
        } catch (IOException e) {
            LOG.error("Unable to deserialize data from topic " + s, e);
            return null;
        }
    }
}

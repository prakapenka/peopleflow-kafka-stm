package localhost.data.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class EmployeeDeserializer implements Deserializer<EmployeeEvent> {

    private final ObjectMapper om = new ObjectMapper();
    Logger LOG = LoggerFactory.getLogger(EmployeeDeserializer.class);

    @Override
    public EmployeeEvent deserialize(String s, byte[] bytes) {
        try {
            return om.readValue(bytes, EmployeeEvent.class);
        } catch (IOException e) {
            LOG.error("Unable to deserialize data from topic " + s, e);
            return null;
        }
    }
}

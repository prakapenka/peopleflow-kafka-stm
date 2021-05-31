package localhost.data.kafka;

import localhost.data.States;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class StatesDeserializer implements Deserializer<States> {

    Logger LOG = LoggerFactory.getLogger(StatesDeserializer.class);

    @Override
    public States deserialize(String topic, byte[] data) {
        if (data == null || data.length < 1) {
            return null;
        }

        try {
            return States.valueOf(new String(data, StandardCharsets.UTF_8));
        } catch (Exception e) {
            LOG.error("Unable to read key from topic: " + topic, e);
            return null;
        }
    }
}

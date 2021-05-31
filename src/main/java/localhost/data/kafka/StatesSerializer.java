package localhost.data.kafka;

import localhost.data.States;
import org.apache.kafka.common.serialization.Serializer;

import java.nio.charset.StandardCharsets;

public class StatesSerializer implements Serializer<States> {

    @Override
    public byte[] serialize(String topic, States data) {
        if (data == null) {
            return new byte[]{};
        }
        return data.toString().getBytes(StandardCharsets.UTF_8);
    }
}

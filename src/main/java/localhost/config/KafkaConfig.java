package localhost.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter(AccessLevel.MODULE)
@Getter
@Configuration
@ConfigurationProperties("kafka")
public class KafkaConfig {

    private String bootstrapServers;
    private String topic;
    private String group;
}

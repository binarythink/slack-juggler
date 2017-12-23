package io.kindler.slack.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "slack.config")
public class SlackProperties {
    public String token;
    public String botName;
    public String botImage;
}

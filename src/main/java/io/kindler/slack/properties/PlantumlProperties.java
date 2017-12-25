package io.kindler.slack.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "slack.bot.plantuml")
public class PlantumlProperties {
    private String pattern = "(?<key>(@startuml)\\n(.*?)\\n(@enduml))";
    private String filepath;
    private String url;
}

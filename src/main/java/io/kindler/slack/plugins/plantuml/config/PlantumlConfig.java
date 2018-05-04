package io.kindler.slack.plugins.plantuml.config;

import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class PlantumlConfig {
    @Bean(name = "plantumlBot")
    SlackChatConfiguration plantUmlBot() {
        return SlackChatConfiguration.getConfiguration()
                .withName("PlnatUml Juggler")
                .withIcon("http://plantuml.com/logo3.png");
    }
}

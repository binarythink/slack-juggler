package io.kindler.slack.plugins.jira.config;

import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class JiraConfig {
    @Bean(name = "jiraBot")
    SlackChatConfiguration jiraConnectBot() {
        return SlackChatConfiguration.getConfiguration()
                .withName("JIRA Juggler")
                .withIcon("https://lh3.googleusercontent.com/GkgChJMixx9JAmoUi1majtfpjg1Ra86gZR0GCehJfVcOGQI7Ict_TVafXCtJniVn3R0");
    }
}

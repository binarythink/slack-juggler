package io.kindler.slack.config;

import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GithubConfig {

    @Bean(name = "githubBot")
    SlackChatConfiguration jiraConnectBot() {
        return SlackChatConfiguration.getConfiguration()
                .withName("Github Juggler")
                .withIcon("https://www.dev-metal.com/wp-content/uploads/2014/01/github-logo-octocat-1.jpg");
    }
}

package io.kindler.slack.config;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackConnectedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackMessageDeletedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackMessageUpdatedListener;
import io.kindler.slack.listener.SlackConnectedListenerImpl;
import io.kindler.slack.listener.SlackMessageDeletedListenerImpl;
import io.kindler.slack.listener.SlackMessagePostedListenerImpl;
import io.kindler.slack.listener.SlackMessageUpdatedListenerImpl;
import io.kindler.slack.properties.SlackProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Slf4j
@Configuration
public class SlackConfig {

    private final SlackProperties properties;

    @Autowired
    public SlackConfig(SlackProperties properties) {
        this.properties = properties;
    }

    @Bean
    SlackSession slackSession() throws IOException {
        // slack session create
        log.debug("slack properties : {}", properties);
        SlackSession slackSession = SlackSessionFactory.createWebSocketSlackSession(properties.token);
        slackSession.connect();

        // slack session add listener
        slackSession.addMessagePostedListener(eventMessagePosted());
        slackSession.addMessageUpdatedListener(eventMessageUpdate());
        slackSession.addMessageDeletedListener(eventMessageDelete());
        return slackSession;
    }

    @Bean
    SlackMessageDeletedListener eventMessageDelete() {
        return new SlackMessageDeletedListenerImpl();
    }

    @Bean
    SlackMessagePostedListener eventMessagePosted() {
        return new SlackMessagePostedListenerImpl();
    }

    @Bean
    SlackConnectedListener eventConnected() {
        return new SlackConnectedListenerImpl();
    }

    @Bean
    SlackMessageUpdatedListener eventMessageUpdate() {
        return new SlackMessageUpdatedListenerImpl();
    }
}

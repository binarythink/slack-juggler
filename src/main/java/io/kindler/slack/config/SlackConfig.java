package io.kindler.slack.config;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackConnectedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackMessageUpdatedListener;
import io.kindler.slack.listener.SlackConnectedListenerImpl;
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

    @Autowired
    SlackProperties properties;

    @Bean
    SlackSession slackSession() throws IOException {
        // slack session create
        SlackSession slackSession = SlackSessionFactory.createWebSocketSlackSession(properties.token);
        slackSession.connect();

        // slack session join channel
        slackSession.joinChannel("general");

        // slack session add listener
        slackSession.addMessagePostedListener(eventMessagePosted());
        slackSession.addMessageUpdatedListener(eventMessageUpdated());
        slackSession.addSlackConnectedListener(eventConnected());

        return slackSession;
    }

    @Bean
    SlackMessagePostedListener eventMessagePosted() {
        return new SlackMessagePostedListenerImpl();
    }

    @Bean
    SlackMessageUpdatedListener eventMessageUpdated() {
        return new SlackMessageUpdatedListenerImpl();
    }

    @Bean
    SlackConnectedListener eventConnected() {
        return new SlackConnectedListenerImpl();
    }


}

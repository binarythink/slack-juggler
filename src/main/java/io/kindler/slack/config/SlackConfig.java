package io.kindler.slack.config;

import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.impl.SlackSessionFactory;
import com.ullink.slack.simpleslackapi.listeners.SlackConnectedListener;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import io.kindler.slack.listener.SlackConnectedListenerImpl;
import io.kindler.slack.listener.SlackMessagePostedListenerImpl;
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
        slackSession.addSlackConnectedListener(eventConnected());

        return slackSession;
    }

    @Bean
    SlackMessagePostedListener eventMessagePosted() {
        return new SlackMessagePostedListenerImpl();
    }

    @Bean
    SlackConnectedListener eventConnected() {
        return new SlackConnectedListenerImpl();
    }

    @Bean(name = "plantUmlBot")
    SlackChatConfiguration plantUmlBot() {
        return SlackChatConfiguration.getConfiguration()
                .withName("슾비서 : plantUml")
                .withIcon("http://plantuml.com/logo3.png");
    }

    @Bean(name = "jiraConnectBot")
    SlackChatConfiguration jiraConnectBot() {
        return SlackChatConfiguration.getConfiguration()
                .withName("슾비서 : GoJIRA")
                .withIcon("https://lh3.googleusercontent.com/GkgChJMixx9JAmoUi1majtfpjg1Ra86gZR0GCehJfVcOGQI7Ict_TVafXCtJniVn3R0");
    }

}

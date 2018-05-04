package io.kindler.slack.listener;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackConnected;
import com.ullink.slack.simpleslackapi.listeners.SlackConnectedListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SlackConnectedListenerImpl implements SlackConnectedListener {

    @Override
    public void onEvent(SlackConnected slackConnected, SlackSession slackSession) {
        SlackChannel generalChannel = slackSession.findChannelByName("boot-test");
        slackSession.sendMessage(generalChannel, "봇이 출근 했습니다.");
    }
}

package io.kindler.slack.listener;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessageUpdated;
import com.ullink.slack.simpleslackapi.listeners.SlackMessageUpdatedListener;
import io.kindler.slack.plugins.jira.component.JiraUtils;
import io.kindler.slack.plugins.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

@Slf4j
public class SlackMessageUpdatedListenerImpl implements SlackMessageUpdatedListener {
    @Autowired
    private RedisService redisService;

    @Autowired
    private JiraUtils jiraUtils;

    @Autowired
    @Qualifier(value = "jiraBot")
    private SlackChatConfiguration chatConfiguration;

    @Override
    public void onEvent(SlackMessageUpdated event, SlackSession session) {
        SlackChannel channel = event.getChannel();
        String timestamp = event.getMessageTimestamp();

        //업데이트 된 메시지에 히스토리가 있는지 확인한다
        List<String> history = redisService.getHistory(channel.getName(), timestamp);

        //히스토리가 없으면 작업을 건너뛴다
        if (history.isEmpty()) return;

        //히스토리가 있으면 메시지를 삭제한다
        for (String ts : history) {
            session.deleteMessage(ts, channel);
        }

        // TODO 봇 사용자 인지 확인한다

        //신규 메시지를 등록한다
//        String newMessage = event.getNewMessage();
//
//        List<String> keyList = jiraUtils.extractionKeys(newMessage);
//        keyList.stream()
//                .map(jiraUtils::getIssue)
//                .map(x -> jiraUtils.makeMessageShort(x, timestamp))
//                .forEach(x -> session.sendMessage(channel, x, chatConfiguration));
    }
}

package io.kindler.slack.listener;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessageUpdated;
import com.ullink.slack.simpleslackapi.listeners.SlackMessageUpdatedListener;
import io.kindler.slack.service.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class SlackMessageUpdatedListenerImpl implements SlackMessageUpdatedListener {
    @Autowired
    private RedisService redisService;

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
            log.error("{}", ts);
            session.deleteMessage(ts, channel);
        }

        //신규 메시지를 등록한다
        String newMessage = event.getNewMessage();
    }
}

package io.kindler.slack.service.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class RedisService {
    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * jira 메시지의 동작 history 를 기록한다
     *
     * @param channelId
     * @param threadTs
     * @param replyTs
     * @return
     */
    public Long setHistory(String channelId, String threadTs, String replyTs) {
        return redisTemplate.opsForList().leftPush("slack:thread:" + channelId + ":" + threadTs, replyTs);
    }

    /**
     * jira 메시지의 동작 history 를 조회한다
     *
     * @param channelId
     * @param threadTs
     * @return
     */
    public List<String> getHistory(String channelId, String threadTs) {
        return redisTemplate.opsForList().range("slack:thread:" + channelId + ":" + threadTs, 0, -1);
    }

    /**
     * jira 메시지의 동작 history 를 삭제한다
     *
     * @param channelId
     * @param threadTs
     * @return
     */
    public void delHistory(String channelId, String threadTs) {
        List<String> strings = Arrays.asList("slack:thread:" + channelId + ":" + threadTs);
        redisTemplate.delete(strings);
    }
}

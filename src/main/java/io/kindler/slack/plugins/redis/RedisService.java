package io.kindler.slack.plugins.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
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
    public void setHistory(String channelId, String threadTs, String replyTs) {
        String key = "slack:thread:" + channelId + ":" + threadTs;
        redisTemplate.opsForList().leftPush(key, replyTs);
        redisTemplate.expire(key, 3, TimeUnit.DAYS);
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

    /**
     * jira id 로 slack 정보를 획득한다
     *
     * @param key jira id
     * @return
     */
    public String getUser(String key) {
        return redisTemplate.opsForValue().get("slack:user:" + key);
    }
}

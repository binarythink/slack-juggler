package io.kindler.slack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("redis")
public class RedisController {
    private final StringRedisTemplate redisTemplate;

    @Autowired
    public RedisController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/get/{key}")
    public String get(@PathVariable String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/keys")
    public Set<String> keys() {
        return redisTemplate.keys("*");
    }

    @RequestMapping(method = RequestMethod.GET, value = "list/set")
    public void list() {
        ListOperations<String, String> list = redisTemplate.opsForList();
        list.leftPush("test", "1");
    }
}

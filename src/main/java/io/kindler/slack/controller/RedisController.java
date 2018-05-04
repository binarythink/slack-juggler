package io.kindler.slack.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("redis")
public class RedisController {
    @Autowired
    StringRedisTemplate redisTemplate;


    @RequestMapping(method = RequestMethod.GET, value = "/get/{key}")
    public String get(@PathVariable String key) {
        return redisTemplate.opsForValue().get(key);
    }
}

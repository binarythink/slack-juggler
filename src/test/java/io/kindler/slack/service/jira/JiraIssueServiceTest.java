package io.kindler.slack.service.jira;

import io.kindler.slack.config.JiraConfig;
import io.kindler.slack.properties.JiraProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = {JiraConfig.class, JiraProperties.class, JiraIssueService.class}
)
@EnableAutoConfiguration
public class JiraIssueServiceTest {
    @Autowired
    JiraProperties jiraProperties;

    @Autowired
    JiraIssueService service;

    @Test
    public void applicationProperties() {
        log.warn("{}", jiraProperties);
    }

    @Test
    public void isTriggerSingle() {
        Assert.assertEquals(true, service.isTrigger("JRASERVER-1234"));
        Assert.assertEquals(true, service.isTrigger("jraserver-1234"));
    }

    @Test
    public void isTriggerAtStart() {
        Assert.assertEquals(true, service.isTrigger("JRASERVER-12344 checked"));
        Assert.assertEquals(true, service.isTrigger("jraserver-1234 checked"));
    }

    @Test
    public void isTriggerAtEnd() {
        Assert.assertEquals(true, service.isTrigger("check JRASERVER-1234"));
        Assert.assertEquals(true, service.isTrigger("check jraserver-1234"));
    }

    @Test
    public void isTriggerMultiLine() {
        Assert.assertEquals(true, service.isTrigger("JRASERVER-1234\n이거 좀 봐봐"));
        Assert.assertEquals(true, service.isTrigger("jraserver-1234\n이거 좀 봐봐"));
        Assert.assertEquals(true, service.isTrigger("이거 좀 봐봐\nJRASERVER-1234\n이거 좀 봐봐"));
        Assert.assertEquals(true, service.isTrigger("이거 좀 봐봐\njraserver-1234\n이거 좀 봐봐"));
        Assert.assertEquals(true, service.isTrigger("이거 좀 봐봐\nJRASERVER-1234"));
        Assert.assertEquals(true, service.isTrigger("이거 좀 봐봐\njraserver-1234"));
    }
}
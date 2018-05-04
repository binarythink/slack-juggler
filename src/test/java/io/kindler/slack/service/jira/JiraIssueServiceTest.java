package io.kindler.slack.service.jira;

import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import io.kindler.slack.Application;
import io.kindler.slack.properties.JiraProperties;
import io.kindler.slack.properties.SlackProperties;
import io.kindler.slack.service.jira.domain.JiraIssue;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class JiraIssueServiceTest {
    @Autowired
    JiraIssueService service;

    @Autowired
    SlackProperties slackProperties;
    @Autowired
    JiraProperties jiraProperties;

    @Test
    public void testPattern() {
        log.debug("slack properties : {}", slackProperties);
        log.debug("jira properties : {}", jiraProperties);

        String issueKey;
        JiraIssue data = null;
        Matcher matcher = Pattern.compile(jiraProperties.getPattern(), Pattern.CASE_INSENSITIVE).matcher("BQA-265");
        SlackPreparedMessage message;
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                issueKey = matcher.group(i).toUpperCase();
                log.debug("issueKey : {}", issueKey);
            }
        }
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
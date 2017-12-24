package io.kindler.slack.service.jira;

import io.kindler.slack.Application;
import io.kindler.slack.ApplicationTests;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class JiraInformationServiceTest {
    @Autowired
    JiraInformationService service;

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
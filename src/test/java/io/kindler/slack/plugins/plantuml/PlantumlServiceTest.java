package io.kindler.slack.plugins.plantuml;

import io.kindler.slack.Application;
import io.kindler.slack.plugins.plantuml.config.PlantumlProperties;
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
public class PlantumlServiceTest {
    @Autowired
    PlantumlService service;

    @Autowired
    PlantumlProperties properties;

    @Test
    public void isTrigger() {
        String content = "@startuml\n1234->456\n@enduml";

        Pattern compile = Pattern.compile(properties.getPattern());
        Matcher matcher = compile.matcher(content);

        Assert.assertEquals(true, matcher.matches());
    }
}
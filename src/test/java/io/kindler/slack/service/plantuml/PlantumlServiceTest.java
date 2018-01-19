package io.kindler.slack.service.plantuml;

import io.kindler.slack.config.PlantumlConfig;
import io.kindler.slack.properties.PlantumlProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(
        properties = "classpath:application-test.yml",
        classes = {PlantumlConfig.class, PlantumlService.class, PlantumlProperties.class}
)
@EnableAutoConfiguration
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
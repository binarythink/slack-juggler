package io.kindler.slack;

import io.kindler.slack.config.SlackConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {
		Application.class,
		SlackConfig.class
})
@WebAppConfiguration
public class ApplicationTests {

	@Test
	public void contextLoads() {
	}

}

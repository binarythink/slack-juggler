package io.kindler.slack.listener;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import io.kindler.slack.service.github.GithubIssueService;
import io.kindler.slack.service.jira.JiraInformationService;
import io.kindler.slack.service.plantuml.PlantUmlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SlackMessagePostedListenerImpl implements SlackMessagePostedListener {
    @Autowired
    PlantUmlService plantUmlService;

    @Autowired
    JiraInformationService jiraInformationService;

    @Autowired
    GithubIssueService githubIssueService;

    @Override
    public void onEvent(SlackMessagePosted event, SlackSession slackSession) {
        SlackUser sender = event.getSender();
        String content = event.getMessageContent();

        // 메시지를 보낸 사용자가 봇일 경우 무시한다
        if (sender.isBot()) return;

        log.info("[{}] {} says \n{}", event.getTimeStamp(), sender.getId(), content);

        // 서비스 발동 조건을 확인하고 서비스를 실행한다
        if (jiraInformationService.isTrigger(content)) {
            log.debug("run jiraInformationService.");
            jiraInformationService.execute(event, slackSession);
        }
        if (githubIssueService.isTrigger(content)) {
            log.debug("run githubInformationService");
            githubIssueService.execute(event, slackSession);
        }
        if (plantUmlService.isTrigger(content)) {
            log.debug("run plantUmlService.");
            plantUmlService.execute(event, slackSession);
        }
    }
}

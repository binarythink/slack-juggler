package io.kindler.slack.listener;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import io.kindler.slack.config.SlackProperties;
import io.kindler.slack.plugins.github.GithubIssueService;
import io.kindler.slack.plugins.jira.JiraIssueService;
import io.kindler.slack.plugins.plantuml.PlantumlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SlackMessagePostedListenerImpl implements SlackMessagePostedListener {
    @Autowired
    PlantumlService plantumlService;

    @Autowired
    JiraIssueService jiraIssueService;

    @Autowired
    GithubIssueService githubIssueService;

    @Autowired
    SlackProperties properties;

    @Override
    public void onEvent(SlackMessagePosted event, SlackSession slackSession) {
        SlackUser sender = event.getSender();
        String content = event.getMessageContent();

        // 메시지를 보낸 사용자가 봇일 경우 무시한다
        if (sender.isBot()) return;

        // 서비스 발동 조건을 확인하고 서비스를 실행한다
        if (jiraIssueService.isTrigger(content)) {
            jiraIssueService.execute(event, slackSession);
        }

        /*
        if (githubIssueService.isTrigger(content)) {
            githubIssueService.execute(event, slackSession);
        }
        if (plantumlService.isTrigger(content)) {
            plantumlService.execute(event, slackSession);
        }*/
    }
}

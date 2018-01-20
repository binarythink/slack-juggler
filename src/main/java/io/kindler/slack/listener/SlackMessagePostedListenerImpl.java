package io.kindler.slack.listener;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.SlackUser;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import com.ullink.slack.simpleslackapi.listeners.SlackMessagePostedListener;
import io.kindler.slack.domain.MessageInfo;
import io.kindler.slack.service.github.GithubIssueService;
import io.kindler.slack.service.jira.JiraIssueService;
import io.kindler.slack.service.plantuml.PlantumlService;
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

    @Override
    public void onEvent(SlackMessagePosted event, SlackSession slackSession) {
        log.info("MessagePosted Listener");

        SlackUser sender = event.getSender();
        String content = event.getMessageContent();

        MessageInfo messageInfo = MessageInfo.Builder.newInstance()
                .channel(event.getChannel())
                .user(event.getUser())
                .content(event.getMessageContent())
                .timestamp(event.getTimestamp())
                .threadTimestamp(event.getThreadTimestamp())
                .build();

        // 메시지를 보낸 사용자가 봇일 경우 무시한다
        if (sender.isBot()) return;

        // 서비스 발동 조건을 확인하고 서비스를 실행한다
        if (jiraIssueService.isTrigger(content)) {
            log.info("run JiraIssueService");
            jiraIssueService.execute(messageInfo, event, slackSession);
        }
        if (githubIssueService.isTrigger(content)) {
            log.info("run GitHubIssueService");
            githubIssueService.execute(messageInfo, event, slackSession);
        }
        if (plantumlService.isTrigger(content)) {
            log.info("run PlantumlService");
            plantumlService.execute(messageInfo, event, slackSession);
        }
    }
}

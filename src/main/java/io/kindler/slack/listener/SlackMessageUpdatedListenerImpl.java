package io.kindler.slack.listener;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessageUpdated;
import com.ullink.slack.simpleslackapi.listeners.SlackMessageUpdatedListener;
import io.kindler.slack.domain.MessageInfo;
import io.kindler.slack.properties.PlantumlProperties;
import io.kindler.slack.service.github.GithubIssueService;
import io.kindler.slack.service.jira.JiraIssueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SlackMessageUpdatedListenerImpl implements SlackMessageUpdatedListener {
    @Autowired
    JiraIssueService jiraIssueService;

    @Autowired
    GithubIssueService githubIssueService;

    @Autowired
    PlantumlProperties plantumlProperties;

    @Override
    public void onEvent(SlackMessageUpdated event, SlackSession slackSession) {
        log.info("MessageUpdated Listener");

        MessageInfo messageInfo = MessageInfo.Builder.newInstance()
                .channel(event.getChannel())
                .content(event.getNewMessage())
                .timestamp(event.getMessageTimestamp())
                .build();

        if (jiraIssueService.isTrigger(messageInfo.getContent())) {
            log.info("run JiraIssueService");
            jiraIssueService.execute(messageInfo, event, slackSession);
        }
        if (githubIssueService.isTrigger(messageInfo.getContent())) {
            log.info("run GitHubIssueService");
            githubIssueService.execute(messageInfo, event, slackSession);
        }
    }
}

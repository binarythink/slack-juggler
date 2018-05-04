package io.kindler.slack.plugins.jira;

import com.ullink.slack.simpleslackapi.*;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.kindler.slack.component.SlackFormatter;
import io.kindler.slack.plugins.JugglerService;
import io.kindler.slack.plugins.jira.component.JiraUtils;
import io.kindler.slack.plugins.jira.config.JiraProperties;
import io.kindler.slack.plugins.jira.domain.JiraIssue;
import io.kindler.slack.plugins.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JiraIssueService implements JugglerService<SlackMessagePosted> {
    @Autowired
    JiraProperties properties;

    @Autowired
    JiraUtils jiraUtils;

    @Autowired
    RedisService redisService;

    @Autowired
    @Qualifier(value = "jiraBot")
    private SlackChatConfiguration chatConfiguration;

    @Override
    public void execute(SlackMessagePosted event, SlackSession slackSession) {
        SlackChannel channel = event.getChannel();
        String content = event.getMessageContent();
        String timestamp = event.getThreadTimestamp() != null ? event.getThreadTimestamp() : event.getTimestamp();

        List<String> keyList = jiraUtils.extractionKeys(content);
        log.info("keyList : {}", keyList);
        keyList.stream()
                .map(jiraUtils::getIssue)
                .map(x -> jiraUtils.makeMessageShort(x, timestamp))
                .map(x -> slackSession.sendMessage(channel, x, chatConfiguration))
                .forEach(x -> redisService.setHistory(channel.getName(), timestamp, x.getReply().getTimestamp()));

    }

    public boolean isTrigger(String content) {
        Pattern pattern = Pattern.compile(properties.getPattern(), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }

    private SlackPreparedMessage makeMessage(JiraIssue data, String threadTimestamp) {

        String titleUrl = properties.getScheme() + "://" + properties.getHost() + "/browse/" + data.getKey();

        // 테이터를 획득하지 못한 경우
        if (!data.isFetch()) {
            StringBuffer sb = new StringBuffer();
            sb.append(SlackFormatter.link(data.getKey(), titleUrl));
            sb.append("이슈정보를 획득하지 못했습니다. 이슈번호를 클릭하면 해당 이슈로 연결됩니다.");

            return new SlackPreparedMessage.Builder()
                    .withMessage(sb.toString())
                    .build();
        }

        // 데이터를 획득한 경우
        SlackPreparedMessage.Builder messageBuilder = new SlackPreparedMessage.Builder();

        SlackAttachment attachment = new SlackAttachment(
                data.getKey(),
                "JIRA Issue " + data.getKey(),
                SlackFormatter.codeBock(data.getFields().getSummary())
                , SlackFormatter.italics("이슈번호를 클릭하면 해당 이슈로 연결됩니다.")
        );
        attachment.setTitleLink(titleUrl);
        attachment.setColor("0747a5");
        attachment.addMarkdownIn("text");
        attachment.addMarkdownIn("pretext");
        attachment.addMarkdownIn("fields");
        attachment.setFooter("slack-juggler");
        attachment.setFooterIcon("https://platform.slack-edge.com/img/default_application_icon.png");
        attachment.setTimestamp(System.currentTimeMillis());

        //필드 추가
        attachment.addField("status", SlackFormatter.code(data.getFields().getStatus().getName()), true);
        attachment.addField("assignee", data.getFields().getAssignee() != null ? SlackFormatter.emptyTo(data.getFields().getAssignee().getDisplayName(), "-") : "–", true);
        attachment.addField("labels", data.getFields().getLabels() != null ? SlackFormatter.emptyTo(SlackFormatter.arrayJoin(Arrays.stream(data.getFields().getLabels()).map(SlackFormatter::code).collect(Collectors.toList())), "-") : "–", false);
        attachment.addField("creator", data.getFields().getCreator() != null ? SlackFormatter.emptyTo(data.getFields().getCreator().getDisplayName(), "-") : "-", true);
        attachment.addField("created", data.getFields().getCreated().toString(), true);
        attachment.addField("duedate", data.getFields().getDuedate() != null ? new SimpleDateFormat("yyyy-MM-dd").format(data.getFields().getDuedate()) : "-", true);
        attachment.addField("resolution date", data.getFields().getResolutiondate() != null ? SlackFormatter.emptyTo(data.getFields().getResolutiondate().toString(), "-") : "-", true);

        messageBuilder.withAttachments(Collections.singletonList(attachment));

        if (threadTimestamp != null) {
            messageBuilder.withThreadTimestamp(threadTimestamp);
        }

        return messageBuilder.build();
    }
}

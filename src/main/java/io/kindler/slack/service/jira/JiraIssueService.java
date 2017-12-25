package io.kindler.slack.service.jira;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.kindler.slack.properties.JiraProperties;
import io.kindler.slack.service.JugglerService;
import io.kindler.slack.service.jira.domain.JiraIssue;
import io.kindler.slack.util.SlackFormatter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.tyrus.core.uri.Match;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class JiraIssueService implements JugglerService<SlackMessagePosted> {
    @Autowired
    JiraProperties properties;

    @Autowired
    @Qualifier(value = "jiraBot")
    private SlackChatConfiguration chatConfiguration;

    @Override
    public void execute(SlackMessagePosted event, SlackSession slackSession) {
        String content = event.getMessageContent();

        String issueKey;
        JiraIssue data = null;
        Matcher matcher = Pattern.compile(properties.getPattern(), Pattern.CASE_INSENSITIVE).matcher(content);
        SlackPreparedMessage message;
        while (matcher.find()) {
            issueKey = matcher.group(1).toUpperCase();
            try {
                data = getData(issueKey);
                log.debug("{}", data);
            } catch (RestClientException e) {
                data = new JiraIssue(issueKey);
                e.printStackTrace();
            }
            message = makeMessage(data);
            slackSession.sendMessage(event.getChannel(), message, chatConfiguration);

        }
    }

    public boolean isTrigger(String content) {
        Pattern pattern = Pattern.compile(properties.getPattern(), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }

    private JiraIssue getData(String key) throws RestClientException {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(properties.getScheme())
                .host(properties.getHost())
                .path("/rest/api/{version}/issue/{key}")
                .buildAndExpand(properties.getVersion(), key);

        HttpHeaders headers = new HttpHeaders();
        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, uriComponents.toUri());

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JiraIssue> response = restTemplate.exchange(requestEntity, JiraIssue.class);
        JiraIssue body = response.getBody();
        body.setFetch(true);
        return body;
    }

    /**
     * 슬랙에 보여질 Message Template를 만든다
     *
     * @param data
     * @return
     */
    private SlackPreparedMessage makeMessage(JiraIssue data) {

        String titleUrl = properties.getScheme() + "://" + properties.getHost() + "/browser/" + data.getKey();

        // 테이터를 획득하지 못한 경우
        if (!data.isFetch()) {
            StringBuffer sb = new StringBuffer();
            sb.append(SlackFormatter.link(data.getKey(), titleUrl));
            sb.append(" 이슈정보를 획득하지 못했습니다. 이슈번호를 클릭하면 해당 이슈로 연결됩니다.");

            return new SlackPreparedMessage.Builder()
                    .withMessage(sb.toString())
                    .build();
        }

        // 데이터를 획득한 경우
        SlackAttachment attachment = new SlackAttachment(
                data.getKey(),
                "JIRA Issue", SlackFormatter.codeBock(data.getFields().getSummary())
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
        attachment.addField("assignee", data.getFields().getAssignee() != null ? StringUtils.defaultIfEmpty(data.getFields().getAssignee().getDisplayName(), "-") : "-", true);
        attachment.addField("labels", data.getFields().getLabels() != null ? StringUtils.defaultIfEmpty(String.join(", ", data.getFields().getLabels()), "-") : "-", false);
        attachment.addField("creator", data.getFields().getCreator() != null ? StringUtils.defaultIfEmpty(data.getFields().getCreator().getDisplayName(), "-") : "-", true);
        attachment.addField("created", data.getFields().getCreated().toString(), true);
        attachment.addField("duedate", data.getFields().getDuedate() != null ? StringUtils.defaultIfEmpty(data.getFields().getDuedate().toString(), "-") : "-", true);
        attachment.addField("resolution date", data.getFields().getResolutiondate() != null ? StringUtils.defaultIfEmpty(data.getFields().getResolutiondate().toString(), "-") : "-", true);

        return new SlackPreparedMessage.Builder()
                .withAttachments(Collections.singletonList(attachment))
                .build();
    }

}

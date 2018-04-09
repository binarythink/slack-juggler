package io.kindler.slack.service.jira;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.kindler.slack.properties.JiraProperties;
import io.kindler.slack.service.JugglerService;
import io.kindler.slack.service.jira.domain.JiraIssue;
import io.kindler.slack.service.jira.domain.JiraUser;
import io.kindler.slack.util.SlackFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    @Qualifier(value = "jiraBot")
    private SlackChatConfiguration chatConfiguration;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void execute(SlackMessagePosted event, SlackSession slackSession) {
        String content = event.getMessageContent();

        String issueKey;
        JiraIssue data = null;
        Matcher matcher = Pattern.compile(properties.getPattern(), Pattern.CASE_INSENSITIVE).matcher(content);
        SlackPreparedMessage message;
        while (matcher.find()) {
            for (int i = 1 ; i <= matcher.groupCount() ; i++) {
                issueKey = matcher.group(i).toUpperCase();
                log.debug("{}", issueKey);
                try {
                    data = getData(issueKey);
                    log.debug("{}", data);
                } catch (RestClientException e) {
                    data = new JiraIssue(issueKey);
                    e.printStackTrace();
                }

                if (properties.isForceThread()) {
                    message = makeMessageShort(data, issueKey, event.getThreadTimestamp() != null ? event.getThreadTimestamp() : event.getTimestamp());
                } else {
                    message = makeMessageShort(data, issueKey, event.getThreadTimestamp());
                }
                slackSession.sendMessage(event.getChannel(), message, chatConfiguration);
            }

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
        if (properties.getAuth() != null) {
            headers.put("Authorization", Collections.singletonList("Basic " + properties.getAuth().getToken()));
        }
        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, uriComponents.toUri());

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JiraIssue> response = restTemplate.exchange(requestEntity, JiraIssue.class);
        JiraIssue body = response.getBody();
        body.setFetch(true);
        return body;
    }

    private String convertUserJira2Slack(String jiraUsername) {
        return properties.getMembers().containsKey(jiraUsername) ? properties.getMembers().get(jiraUsername) : jiraUsername;
    }

    private JiraUser getUserData(String username) {
        log.debug(username);
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(properties.getScheme())
                .host(properties.getHost())
                .path("/rest/api/{version}/user")
                .queryParam("username", username)
                .buildAndExpand(properties.getVersion());

        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", Collections.singletonList("Basic " + properties.getAuth().getToken()));
        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, uriComponents.toUri());

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JiraUser> response = restTemplate.exchange(requestEntity, JiraUser.class);
        return response.getBody();
    }

    /**
     * 슬랙에 보여질 Message Template를 만든다
     *
     * @param data
     * @param threadTimestamp
     * @return
     */
    private SlackPreparedMessage makeMessageShort(JiraIssue data, String key, String threadTimestamp) {

        String titleUrl = properties.getScheme() + "://" + properties.getHost() + "/browse/" + data.getKey();

        // 테이터를 획득하지 못한 경우
        if (!data.isFetch()) {
            StringBuffer sb = new StringBuffer();
            sb.append(SlackFormatter.link(data.getKey(), titleUrl));
            sb.append(" 이슈정보를 획득하지 못했습니다. 이슈번호를 클릭하면 해당 이슈로 연결됩니다.");

            return new SlackPreparedMessage.Builder()
                    .withMessage(sb.toString())
                    .withThreadTimestamp(threadTimestamp)
                    .build();
        }

        // 데이터를 획득한 경우
        SlackPreparedMessage.Builder messageBuilder = new SlackPreparedMessage.Builder();
        messageBuilder.withMessage(SlackFormatter.link("[" + key + "]", titleUrl)
                + properties.getPriority().get(data.getFields().getPriority().getName().toLowerCase())
                + SlackFormatter.bold(SlackFormatter.link(SlackFormatter.escape(data.getFields().getSummary()) , titleUrl))
                + " "
                + SlackFormatter.code(data.getFields().getStatus().getName()));
        messageBuilder.withUnfurl(false);

        SlackAttachment attachment = new SlackAttachment(
                null
                , "JIRA Issue " + key
                , null
                , null
        );
        attachment.addMarkdownIn("fields");

        // 참여자 필드 assignee, reporter, po, participants
        List<JiraUser> userList = new ArrayList<>();
        userList.add(data.getFields().getAssignee());
        userList.add(data.getFields().getReporter());
        if (data.getFields().getPo() != null) {
            userList.add(data.getFields().getPo());
        }
        if (data.getFields().getParticipants() != null) {
            userList.addAll(data.getFields().getParticipants());
        }
        String participants = userList.stream()
                .map(JiraUser::getName)
                .map(this::convertUserJira2Slack)
                .distinct()
                .collect(Collectors.joining(","));

        attachment.addField("참여자", participants, true);
        attachment.addField("마감일", data.getFields().getDuedate() != null ? sdf.format(data.getFields().getDuedate()) : "-", true);

        messageBuilder.addAttachment(attachment);

        if (threadTimestamp != null) {
            messageBuilder.withThreadTimestamp(threadTimestamp);
        }

        return messageBuilder.build();
    }

    /**
     * 슬랙에 보여질 Message Template를 만든다
     *
     * @param data
     * @param threadTimestamp
     * @return
     */
    private SlackPreparedMessage makeMessage(JiraIssue data, String key, String threadTimestamp) {

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
                key,
                "JIRA Issue " + key,
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
        attachment.addField("labels", data.getFields().getLabels() != null ? SlackFormatter.emptyTo(SlackFormatter.arrayJoin(Arrays.stream(data.getFields().getLabels()).map(SlackFormatter::code).collect(Collectors.toList())), "-"): "–", false);
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

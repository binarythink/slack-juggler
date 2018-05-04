package io.kindler.slack.plugins.jira.component;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import io.kindler.slack.component.SlackFormatter;
import io.kindler.slack.plugins.jira.config.JiraProperties;
import io.kindler.slack.plugins.jira.domain.JiraIssue;
import io.kindler.slack.plugins.jira.domain.JiraUser;
import io.kindler.slack.plugins.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JiraUtils {
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Autowired
    private JiraProperties properties;

    @Autowired
    private RedisService redisService;


    public JiraIssue getIssue(String key) throws RestClientException {
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
        if (response.getStatusCode() != HttpStatus.OK) {
            return new JiraIssue(key);
        }

        JiraIssue body = response.getBody();
        body.setFetch(true);
        return body;
    }

    private JiraUser getUser(String username) {
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

    public List<String> extractionKeys(String message) {
        Matcher matcher = Pattern.compile(properties.getPattern(), Pattern.CASE_INSENSITIVE).matcher(message);

        List<String> result = new ArrayList<>();
        String key;
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                key = matcher.group(i).toUpperCase();

                result.add(key);
            }
        }

        return result;
    }

    public boolean isTrigger(String content) {
        Pattern pattern = Pattern.compile(properties.getPattern(), Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        return matcher.find();
    }

    public SlackPreparedMessage makeMessageShort(JiraIssue data, String threadTimestamp) {

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
        messageBuilder.withMessage(SlackFormatter.link("[" + data.getKey() + "]", titleUrl)
                + properties.getPriority().get(data.getFields().getPriority().getName().toLowerCase())
                + SlackFormatter.bold(SlackFormatter.link(SlackFormatter.escape(data.getFields().getSummary()), titleUrl))
                + " "
                + SlackFormatter.code(data.getFields().getStatus().getName()));
        messageBuilder.withUnfurl(false);

        SlackAttachment attachment = new SlackAttachment(
                null
                , "JIRA Issue " + data.getKey()
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
                .map(x -> {
                    String user = redisService.getUser(x);
                    return user != null ? "<@" + user + ">" : x;
                })
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

//    private String convertUserJira2Slack(String jiraUsername) {
//        return properties.getMembers().containsKey(jiraUsername) ? properties.getMembers().get(jiraUsername) : jiraUsername;
//    }
}

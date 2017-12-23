package io.kindler.slack.service;

import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.kindler.slack.service.jira.JiraIssue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class JiraConnectService implements JugglerService<SlackMessagePosted> {
    @Value(value = "${slack.bot.jira.pattern}")
    private String pattern;

    @Value(value = "${slack.bot.jira.scheme}")
    private String scheme;

    @Value(value = "${slack.bot.jira.host}")
    private String host;

    @Autowired
    @Qualifier(value = "jiraConnectBot")
    private SlackChatConfiguration chatConfiguration;

    @Override
    public void execute(SlackMessagePosted event, SlackSession slackSession) {
        String content = event.getMessageContent();

        SlackPreparedMessage.Builder builder = new SlackPreparedMessage.Builder();
        String issueKey;
        Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(content);
        while (matcher.find()) {
            issueKey = matcher.group().toUpperCase();
            JiraIssue data = getData(issueKey);
            builder.withMessage("<" + scheme + "://" + host + "/browse/" + issueKey + "|[" + issueKey + "]> " + data.getFields().getSummary());
            slackSession.sendMessage(event.getChannel(), builder.build(), chatConfiguration);
        }
    }

    public boolean isTrigger(String content) {
        Pattern pattern = Pattern.compile(this.pattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        return matcher.matches();
    }

    private JiraIssue getData(String key) {
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(scheme)
                .host(host)
                .path("/rest/api/{version}/issue/{key}")
                .buildAndExpand("latest", key);

        HttpHeaders headers = new HttpHeaders();
        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, uriComponents.toUri());

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JiraIssue> response = restTemplate.exchange(requestEntity, JiraIssue.class);
        return response.getBody();
    }
}

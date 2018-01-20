package io.kindler.slack.service.github;

import com.ullink.slack.simpleslackapi.SlackAttachment;
import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackEvent;
import io.kindler.slack.domain.MessageInfo;
import io.kindler.slack.properties.GithubProperties;
import io.kindler.slack.service.JugglerService;
import io.kindler.slack.service.github.domain.GithubIssue;
import io.kindler.slack.service.github.domain.GithubLabel;
import io.kindler.slack.service.github.domain.GithubUser;
import io.kindler.slack.util.SlackFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Controller
public class GithubIssueService implements JugglerService<SlackEvent> {
    @Autowired
    GithubProperties properties;

    @Autowired
    @Qualifier("githubBot")
    private SlackChatConfiguration chatConfiguration;

    @Override
    public void execute(MessageInfo messageInfo, SlackEvent event, SlackSession slackSession) {
        String content = messageInfo.getContent();

        String issueKey;
        String owner;
        String repo;
        GithubIssue data = null;
        Matcher matcher = Pattern.compile(properties.getPattern()).matcher(content);
        SlackPreparedMessage message;
        if (matcher.matches()) {
            owner = properties.isUseDefaultOwner() ? properties.getRepo().getOwner() : matcher.group("owner");
            repo = matcher.group("repo");
            issueKey = matcher.group("num");
            try {
                data = getData(owner, repo, issueKey);
                log.debug("{}", data);
            } catch (RestClientException e) {
                data = new GithubIssue();
                e.printStackTrace();
            }
            message = makeMessage(data, "[" + owner + "/" + repo + " " + matcher.group("fmtNum") + "]");
            slackSession.sendMessage(messageInfo.getChannel(), message, chatConfiguration);
        }
    }

    private SlackPreparedMessage makeMessage(GithubIssue data, String trigger) {
        // 데이터를 획득한 경우
        SlackAttachment attachment = new SlackAttachment(
                trigger + "\n — " + data.getTitle(),
                "Github Issue " + trigger,
                SlackFormatter.codeBock(SlackFormatter.emptyTo(data.getBody(), "none description")),
                SlackFormatter.italics(trigger + " - 데이터를 정상적으로 획득했습니다.") + "\n" + SlackFormatter.italics("제목을 클릭하면 해당 이슈로 연결됩니다."));
        attachment.setTitleLink(data.getHtmlUrl());
        attachment.setColor("24292e");
        attachment.addMarkdownIn("text");
        attachment.addMarkdownIn("pretext");
        attachment.addMarkdownIn("fields");
        attachment.setFooter("slack-juggler");
        attachment.setFooterIcon("https://platform.slack-edge.com/img/default_application_icon.png");
        attachment.setTimestamp(System.currentTimeMillis());

        //필드 추가
        attachment.addField("status", SlackFormatter.code(data.getState()), true);
        attachment.addField("assignees", SlackFormatter.emptyTo(SlackFormatter.arrayJoin(data.getAssignees().stream().map(GithubUser::getLogin).collect(Collectors.toList())), "-"), true);
        attachment.addField("labels", SlackFormatter.emptyTo(SlackFormatter.arrayJoin(data.getLabels().stream().map(GithubLabel::getName).map(SlackFormatter::code).collect(Collectors.toList()), " "), "-"), false);
        attachment.addField("creator", data.getUser().getLogin(), true);
        attachment.addField("created", data.getCreatedAt().toString(), true);
        if (data.getUpdatedAt() != null) {
            attachment.addField("updated", data.getUpdatedAt().toString(), true);
        }
        if (data.getClosedAt() != null) {
            attachment.addField("closed", data.getClosedAt().toString(), true);
        }

        return new SlackPreparedMessage.Builder()
                .withAttachments(Collections.singletonList(attachment))
                .build();
    }

    private GithubIssue getData(String owner, String repo, String key) throws RestClientException {
        UriComponents uriComponents = UriComponentsBuilder.fromHttpUrl(properties.API)
                .path("/repos/{owner}/{repo}/issues/{number}")
                .buildAndExpand(owner, repo, key);

        HttpHeaders headers = new HttpHeaders();
        headers.put("Authorization", Collections.singletonList("Basic " + properties.getAuth().getToken()));
        RequestEntity<Void> requestEntity = new RequestEntity<>(headers, HttpMethod.GET, uriComponents.toUri());

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<GithubIssue> response = restTemplate.exchange(requestEntity, GithubIssue.class);

        return response.getBody();
    }

    @Override
    public boolean isTrigger(String content) {
        Pattern pattern = Pattern.compile(properties.getPattern());
        Matcher matcher = pattern.matcher(content);
        return matcher.matches();
    }
}

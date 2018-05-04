package io.kindler.slack.service.github;

import io.kindler.slack.Application;
import io.kindler.slack.properties.GithubProperties;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class GithubIssueServiceTest {
    @Autowired
    GithubIssueService service;

    @Autowired
    GithubProperties properties;

    @Test
    public void checkRepoRegex() {

        Pattern pattern = Pattern.compile(GithubProperties.REPO_REGEX);

        //단독
        Matcher matcher = pattern.matcher("slack-juggle #1");
        if (matcher.matches()) {
            Assert.assertEquals("slack-juggle", matcher.group("repo"));
            Assert.assertEquals("#1", matcher.group("fmtNum"));
            Assert.assertEquals("1", matcher.group("num"));
        }

        //앞
        matcher = pattern.matcher("slack-juggle #1 진행상황 업데이트 해주세요.");
        if (matcher.matches()) {
            Assert.assertEquals("slack-juggle", matcher.group("repo"));
            Assert.assertEquals("#1", matcher.group("fmtNum"));
            Assert.assertEquals("1", matcher.group("num"));
        }

        //중간
        matcher = pattern.matcher("이슈 확인 하자 slack-juggle #1 봐줘");
        if (matcher.matches()) {
            Assert.assertEquals("slack-juggle", matcher.group("repo"));
            Assert.assertEquals("#1", matcher.group("fmtNum"));
            Assert.assertEquals("1", matcher.group("num"));
        }

        //뒤
        matcher = pattern.matcher("이슈 확인 부탁드립니다. slack-juggle #1");
        if (matcher.matches()) {
            Assert.assertEquals("slack-juggle", matcher.group("repo"));
            Assert.assertEquals("#1", matcher.group("fmtNum"));
            Assert.assertEquals("1", matcher.group("num"));
        }
    }

    @Test
    public void checkFullRegex() {

        Pattern pattern = Pattern.compile(GithubProperties.FULL_REGEX);

        //단독
        Matcher matcher = pattern.matcher("binarythink/slack-juggle #1");
        if (matcher.matches()) {
            Assert.assertEquals("binarythink", matcher.group("own"));
            Assert.assertEquals("slack-juggle", matcher.group("repo"));
            Assert.assertEquals("#1", matcher.group("fmtNum"));
            Assert.assertEquals("1", matcher.group("num"));
        }

        //앞
        matcher = pattern.matcher("binarythink/slack-juggle #1 진행상황 업데이트 해주세요.");
        if (matcher.matches()) {
            Assert.assertEquals("binarythink", matcher.group("own"));
            Assert.assertEquals("slack-juggle", matcher.group("repo"));
            Assert.assertEquals("#1", matcher.group("fmtNum"));
            Assert.assertEquals("1", matcher.group("num"));
        }

        //중간
        matcher = pattern.matcher("이슈 확인 하자 binarythink/slack-juggle #1 봐줘");
        if (matcher.matches()) {
            Assert.assertEquals("binarythink", matcher.group("own"));
            Assert.assertEquals("slack-juggle", matcher.group("repo"));
            Assert.assertEquals("#1", matcher.group("fmtNum"));
            Assert.assertEquals("1", matcher.group("num"));
        }

        //뒤
        matcher = pattern.matcher("이슈 확인 부탁드립니다. binarythink/slack-juggle #1");
        if (matcher.matches()) {
            Assert.assertEquals("binarythink", matcher.group("own"));
            Assert.assertEquals("slack-juggle", matcher.group("repo"));
            Assert.assertEquals("#1", matcher.group("fmtNum"));
            Assert.assertEquals("1", matcher.group("num"));
        }
    }

    @Test
    public void isTrigger() {
    }
}
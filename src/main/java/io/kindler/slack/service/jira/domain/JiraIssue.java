package io.kindler.slack.service.jira.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class JiraIssue {
    private boolean fetch = false;
    private String id;
    private String self;
    private String key;
    private JiraIssueFields fields;

    public JiraIssue(String key) {
        this.key = key;
    }
}

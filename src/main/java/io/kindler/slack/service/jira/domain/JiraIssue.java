package io.kindler.slack.service.jira.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class JiraIssue {
    private String id;
    private String self;
    private String key;
    private JiraIssueFields fields;
}

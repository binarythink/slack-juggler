package io.kindler.slack.service.jira.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class JiraUser {
    private String key;
    private String name;
    private String displayName;
}

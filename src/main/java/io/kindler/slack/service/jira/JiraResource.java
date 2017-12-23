package io.kindler.slack.service.jira;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class JiraResource {
    private String id;
    private String name;
    private String description;
}

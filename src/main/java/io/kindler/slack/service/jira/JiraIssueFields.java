package io.kindler.slack.service.jira;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@ToString(callSuper = true)
public class JiraIssueFields {
    private Date created;
    private Date updated;
    private Date resolutiondate;

    private String summary;
    private JiraResource resolution;
    private JiraResource status;
    private JiraUser creator;
    private JiraUser assignee;
    private JiraUser reporter;
}

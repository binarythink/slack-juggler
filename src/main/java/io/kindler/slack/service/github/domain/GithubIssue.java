package io.kindler.slack.service.github.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@ToString
public class GithubIssue {
    private Long id;
    @JsonProperty("html_url")
    private String htmlUrl;
    private Long number;
    private String state;
    private String title;
    private String body;
    private GithubUser user;
    private List<GithubLabel> labels = new ArrayList<>();
    private GithubUser assignee;
    private List<GithubUser> assignees = new ArrayList<>();
    private Boolean locked;
    @JsonProperty("closed_at")
    private Date closedAt;
    @JsonProperty("created_at")
    private Date createdAt;
    @JsonProperty("updated_at")
    private Date updatedAt;
    @JsonProperty("closed_by")
    private GithubUser closedBy;
}


package io.kindler.slack.service.github.domain;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class GithubLabel {
    private Long id;
    private String url;
    private String name;
    private String color;
}

package io.kindler.slack.plugins.github.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Data
@Component
@ConfigurationProperties(prefix = "slack.bot.github")
public class GithubProperties {
    public static final String API = "https://api.github.com";
    public static final String REPO_REGEX = "(?<repo>[\\w-]+)\\s(?<fmtNum>#(?<num>\\d+))";
    public static final String FULL_REGEX = "(?<own>[\\w-]+)\\/(?<repo>[\\w-]+)\\s(?<fmtNum>#(?<num>\\d+))";

    private boolean useDefaultOwner = true;
    private Auth auth;
    private Repo repo;

    public String getPattern() {
        return useDefaultOwner ? REPO_REGEX : FULL_REGEX;
    }

    @Data
    public static class Auth {
        private String username;
        private String password;

        public String getToken() {
            return Base64.getEncoder().encodeToString(String.format("%s:%s", username, password).getBytes());
        }

    }
    @Data
    public static class Repo {
        private String owner;
        private String repo;
    }
}

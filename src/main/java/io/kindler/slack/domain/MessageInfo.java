package io.kindler.slack.domain;

import com.ullink.slack.simpleslackapi.SlackChannel;
import com.ullink.slack.simpleslackapi.SlackUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class MessageInfo {
    private SlackUser user;
    private SlackChannel channel;
    private String content;
    private String timestamp;
    private String threadTimestamp;

    @Data
    @ToString
    public static class Builder {
        MessageInfo obj;

        private Builder() {
            this.obj = new MessageInfo();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder channel(SlackChannel channel) {
            obj.setChannel(channel);
            return this;
        }

        public Builder user(SlackUser user) {
            obj.setUser(user);
            return this;
        }

        public Builder content(String content) {
            obj.setContent(content);
            return this;
        }

        public Builder timestamp(String timestamp) {
            obj.setTimestamp(timestamp);
            return this;
        }

        public Builder threadTimestamp(String threadTimestamp) {
            obj.setThreadTimestamp(threadTimestamp);
            return this;
        }

        public MessageInfo build() {
            return this.obj;
        }
    }
}

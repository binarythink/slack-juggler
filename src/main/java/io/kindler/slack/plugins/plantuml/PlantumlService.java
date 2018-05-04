package io.kindler.slack.plugins.plantuml;

import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.kindler.slack.plugins.JugglerService;
import io.kindler.slack.plugins.plantuml.config.PlantumlProperties;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PlantumlService implements JugglerService<SlackMessagePosted> {

    @Autowired
    @Qualifier(value = "plantumlBot")
    private SlackChatConfiguration chatConfiguration;

    @Autowired
    private PlantumlProperties properties;

    @Override
    public void execute(SlackMessagePosted event, SlackSession slackSession) {
        String content = event.getMessageContent();

        Pattern pattern = Pattern.compile(properties.getPattern());
        Matcher matcher = pattern.matcher(content);
        matcher.matches();

        String key = matcher.group("key");
        try {
            File file = new File(properties.getFilepath(), event.getTimeStamp().concat(".png"));
            FileOutputStream out = new FileOutputStream(file);
            SourceStringReader reader = new SourceStringReader(HtmlUtils.htmlUnescape(key));
            reader.generateImage(out, new FileFormatOption(FileFormat.PNG, false));
            out.flush();
            out.close();

            SlackPreparedMessage preparedMessage = new SlackPreparedMessage.Builder()
                    .withMessage(properties.getUrl() + file.getName())
                    .build();

            slackSession.sendMessage(event.getChannel(), preparedMessage, chatConfiguration);
        } catch (IOException e) {
            e.printStackTrace();
            slackSession.sendMessage(event.getChannel(), "오류");
        }
    }

    @Override
    public boolean isTrigger(String content) {
        Pattern pattern = Pattern.compile(properties.getPattern());
        Matcher matcher = pattern.matcher(content);
        return matcher.matches();
    }
}

package io.kindler.slack.service.plantuml;

import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackEvent;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.kindler.slack.domain.MessageInfo;
import io.kindler.slack.properties.PlantumlProperties;
import io.kindler.slack.service.JugglerService;
import io.kindler.slack.util.SlackFormatter;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.plantuml.FileFormat;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.SourceStringReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PlantumlService implements JugglerService<SlackEvent> {

    @Autowired
    @Qualifier(value = "plantumlBot")
    private SlackChatConfiguration chatConfiguration;

    @Autowired
    private PlantumlProperties properties;

    @Override
    public void execute(MessageInfo messageInfo, SlackEvent event, SlackSession slackSession) {
        String content = messageInfo.getContent();

        Pattern pattern = Pattern.compile(properties.getPattern());
        Matcher matcher = pattern.matcher(content);
        matcher.matches();

        String key = matcher.group("key");
        try {
            File file = new File(properties.getFilepath(), messageInfo.getTimestamp().concat(".png"));
            FileOutputStream out = new FileOutputStream(file);
            SourceStringReader reader = new SourceStringReader(HtmlUtils.htmlUnescape(key));
            reader.generateImage(out, new FileFormatOption(FileFormat.PNG, false));
            out.flush();
            out.close();

            SlackPreparedMessage preparedMessage = new SlackPreparedMessage.Builder()
                    .withMessage(properties.getUrl() + file.getName())
                    .build();

            slackSession.sendMessage(messageInfo.getChannel(), preparedMessage, chatConfiguration);
        } catch (IOException e) {
            e.printStackTrace();
            slackSession.sendMessage(messageInfo.getChannel(), "오류");
        }
    }

    @Override
    public boolean isTrigger(String content) {
        Pattern pattern = Pattern.compile(properties.getPattern());
        Matcher matcher = pattern.matcher(content);
        return matcher.matches();
    }
}

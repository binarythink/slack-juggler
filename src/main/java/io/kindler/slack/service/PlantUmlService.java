package io.kindler.slack.service;

import com.ullink.slack.simpleslackapi.SlackChatConfiguration;
import com.ullink.slack.simpleslackapi.SlackPreparedMessage;
import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
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
import java.util.regex.Pattern;

@Slf4j
@Service
public class PlantUmlService implements JugglerService<SlackMessagePosted> {

    @Autowired
    @Qualifier(value = "plantUmlBot")
    private SlackChatConfiguration chatConfiguration;

    @Value(value = "${slack.bot.plantuml.pattern}")
    private String pattern;

    @Value(value = "${slack.bot.plantuml.filepath}")
    private String filePath;

    @Value(value = "${slack.bot.plantuml.url}")
    private String url;


    @Override
    public void execute(SlackMessagePosted event, SlackSession slackSession) {
        try {
            File file = new File(filePath, event.getTimeStamp().concat(".png"));
            FileOutputStream out = new FileOutputStream(file);
            SourceStringReader reader = new SourceStringReader(HtmlUtils.htmlUnescape(event.getMessageContent()));
            String desc = reader.generateImage(out, new FileFormatOption(FileFormat.PNG, false));
            out.flush();
            out.close();

            SlackPreparedMessage preparedMessage = new SlackPreparedMessage.Builder()
                    .withMessage("<http://example.com" + file.getAbsolutePath() + "|file>")
                    .build();

            slackSession.sendMessage(event.getChannel(), preparedMessage, chatConfiguration);
        } catch (IOException e) {
            e.printStackTrace();
            slackSession.sendMessage(event.getChannel(), "오류");
        }
    }

    @Override
    public boolean isTrigger(String content) {
        return Pattern.matches(pattern, content);
    }
}

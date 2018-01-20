package io.kindler.slack.service;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackEvent;
import com.ullink.slack.simpleslackapi.events.SlackMessagePosted;
import io.kindler.slack.domain.MessageInfo;

public interface JugglerService<T extends SlackEvent> {

    /**
     * 서비스의 본연의 업무를 수행한다
     *
     * @param event
     * @param slackSession
     */
    void execute(MessageInfo messageInfo, T event, SlackSession slackSession);

    /**
     * 발동 조건을 검사한다
     *
     * @param   content
     *          사용자의 입력 메시지
     *
     * @return 실행 조건에 부합할 경우 true, 부합하시 않을 경우 false 를 반환한다
     */
    boolean isTrigger(String content);
}

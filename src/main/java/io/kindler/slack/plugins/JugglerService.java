package io.kindler.slack.plugins;

import com.ullink.slack.simpleslackapi.SlackSession;
import com.ullink.slack.simpleslackapi.events.SlackEvent;

public interface JugglerService<T extends SlackEvent> {

    /**
     * 서비스의 본연의 업무를 수행한다
     *
     * @param event slack event
     * @param slackSession slack session
     */
    void execute(T event, SlackSession slackSession);

    /**
     * 발동 조건을 검사한다
     *
     * @param content message
     * @return 실행 조건에 부합할 경우 true, 부합하시 않을 경우 false 를 반환한다
     */
    boolean isTrigger(String content);
}

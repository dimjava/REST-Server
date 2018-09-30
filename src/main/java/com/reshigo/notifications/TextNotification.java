package com.reshigo.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

/**
 * Created by dmitry103 on 06/06/17.
 */
@Component
public class TextNotification extends Notification {

    public TextNotification() {}

    public TextNotification(String title, String body) {
        setTitle(title);
        setBody(body);
    }

    protected NotificationTypeEnum type = NotificationTypeEnum.TEXT;

    protected String title;

    protected String body;

    protected boolean show = true;

    @Override
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public NotificationTypeEnum getType() {
        return type;
    }

    @Override
    public String getText() {
        return body;
    }

    public void setShow(boolean b) {
        this.show = b;
    }

    public boolean getShow() {
        return this.show;
    }

    public String convert() throws Exception {
        return new ObjectMapper().writeValueAsString(this);
    }
}

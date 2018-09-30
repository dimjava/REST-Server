package com.reshigo.notifications;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by dmitry103 on 07/06/17.
 */
public class LastReadMessageNotification extends Notification {

    public LastReadMessageNotification(Long chatId, Long lastId) {
        setChatId(chatId);
        setLastId(lastId);
    }

    private Long chatId;

    private Long lastId;

    private boolean show = false;

    @Override
    public NotificationTypeEnum getType() {
        return NotificationTypeEnum.LAST_READ;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public Long getLastId() {
        return lastId;
    }

    public void setLastId(Long lastId) {
        this.lastId = lastId;
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

    public String getText() {
        return null;
    }
}

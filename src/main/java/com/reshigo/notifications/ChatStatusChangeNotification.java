package com.reshigo.notifications;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reshigo.model.entity.permanent.ChatStatusEnum;

/**
 * Created by dmitry103 on 03/10/2017.
 */
public class ChatStatusChangeNotification extends Notification {
    @Override
    public NotificationTypeEnum getType() {
        return NotificationTypeEnum.CHAT_STATUS;
    }

    public ChatStatusChangeNotification(Long id, ChatStatusEnum status) {
        this.id = id;
        this.status = status;
    }

    private Long id;

    private boolean show = false;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private ChatStatusEnum status;

    @Override
    public String getText() {
        return null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setShow(boolean b) {
        this.show = b;
    }

    public boolean getShow() {
        return this.show;
    }

    @Override
    public String convert() throws Exception {
        return new ObjectMapper().writeValueAsString(this);
    }

    public ChatStatusEnum getStatus() {
        return status;
    }

    public void setStatus(ChatStatusEnum status) {
        this.status = status;
    }
}

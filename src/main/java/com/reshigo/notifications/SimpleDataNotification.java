package com.reshigo.notifications;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SimpleDataNotification extends TextNotification {

    public SimpleDataNotification(String title, String body, SimpleNotificationData data) {
        this.title = title;
        this.body = body;
        this.data = data;
    }

    @JsonIgnore
    private SimpleNotificationData data;

    @Override
    public SimpleNotificationData getPushData() {
        return this.data;
    }
}

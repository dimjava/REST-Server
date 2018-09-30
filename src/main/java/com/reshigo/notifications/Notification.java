package com.reshigo.notifications;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by dmitry103 on 06/06/17.
 */
public abstract class Notification implements Cloneable {
    public abstract NotificationTypeEnum getType();

    public abstract String getText();

    public abstract boolean getShow();

    public String getTitle() {
        return "";
    }

    public String getSound() {
        return "default";
    }

    @JsonIgnore
    public SimpleNotificationData getPushData() {
        return null;
    }

    public abstract String convert() throws Exception;

    public static <T extends Notification> T clone(T t) throws CloneNotSupportedException {
        return (T) t.clone();
    }
}

package com.reshigo.notifications;

/**
 * Created by dmitry103 on 28/06/17.
 */
public class SyncNotification extends Notification {
    @Override
    public NotificationTypeEnum getType() {
        return NotificationTypeEnum.SYNC;
    }

    @Override
    public String getText() {
        return null;
    }

    public boolean getShow() {
        return false;
    }

    @Override
    public String convert() throws Exception {
        return null;
    }
}

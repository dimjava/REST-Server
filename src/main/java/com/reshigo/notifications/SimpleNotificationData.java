package com.reshigo.notifications;

public class SimpleNotificationData {
    public SimpleNotificationData(Long id, NotificationTypeEnum type) {
        setId(id);
        setType(type);
    }

    private Long id;

    private NotificationTypeEnum type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public NotificationTypeEnum getType() {
        return type;
    }

    public void setType(NotificationTypeEnum type) {
        this.type = type;
    }
}

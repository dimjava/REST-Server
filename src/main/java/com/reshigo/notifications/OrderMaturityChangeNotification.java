package com.reshigo.notifications;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by dmitry103 on 06/06/17.
 */
public class OrderMaturityChangeNotification extends Notification {

    public OrderMaturityChangeNotification(Long id, Long maturity) {
        setId(id);
        setMaturity(maturity);
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private NotificationTypeEnum type = NotificationTypeEnum.ORDER_MATURITY;

    private Long id;

    private Long maturity;

    private boolean show = true;

    @JsonIgnore
    @Override
    public SimpleNotificationData getPushData() {
        return new SimpleNotificationData(id, type);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMaturity() {
        return maturity;
    }

    public void setMaturity(Long maturity) {
        this.maturity = maturity;
    }

    public NotificationTypeEnum getType() {
        return type;
    }

    public String getText() {
        return "Вам добавили времени";
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

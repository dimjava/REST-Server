package com.reshigo.notifications;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by dmitry103 on 28/06/17.
 */
public class PriceUpdateNotification extends Notification {
    @Override
    public NotificationTypeEnum getType() {
        return NotificationTypeEnum.PRICE_UPDATE;
    }

    public PriceUpdateNotification(Long orderId, Long price) {
        setOrderId(orderId);
        setPrice(price);
    }

    public PriceUpdateNotification(Long orderId, Long price, boolean show) {
        setOrderId(orderId);
        setPrice(price);
        setShow(show);
    };

    private Long orderId;

    private Long price;

    private boolean show = false;

    @JsonIgnore
    @Override
    public SimpleNotificationData getPushData() {
        return new SimpleNotificationData(orderId, getType());
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    @Override
    public String getText() {
        if (show) {
            return "Цена заказа изменена. Новое значение – " + price.toString() + " рублей";
        }

        return null;
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

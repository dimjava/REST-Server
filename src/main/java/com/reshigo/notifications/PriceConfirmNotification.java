package com.reshigo.notifications;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by dmitry103 on 28/06/17.
 */
public class PriceConfirmNotification extends Notification {

    public PriceConfirmNotification(Long orderId, Long price, Long suggestionId) {
        setOrderId(orderId);
        setPrice(price);
        setSuggestionId(suggestionId);
    }

    @Override
    public NotificationTypeEnum getType() {
        return NotificationTypeEnum.PRICE_CONFIRM;
    }

    private Long orderId;

    private Long price;

    private Long suggestionId;

    private boolean show = true;

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
        return "Пользователь откликнулся на Ваше предложение по цене. Заказ автоматически зарезервирован.";
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

    public Long getSuggestionId() {
        return suggestionId;
    }

    public void setSuggestionId(Long suggestionId) {
        this.suggestionId = suggestionId;
    }
}

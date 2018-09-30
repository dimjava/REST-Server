package com.reshigo.notifications;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reshigo.model.entity.User;

/**
 * Created by dmitry103 on 28/06/17.
 */
public class PriceSuggestNotification extends Notification {

    public PriceSuggestNotification() {}

    public PriceSuggestNotification(Long orderId, Long price, User solver) {
        setOrderId(orderId);
        setPrice(price);
    }

    @Override
    public NotificationTypeEnum getType() {
        return NotificationTypeEnum.PRICE_SUGGEST;
    }

    private Long orderId;

    private Long suggestionId;

    private Long price;

    private boolean show = true;

    private String comment = "";

    @JsonIgnoreProperties({"email", "phone", "payments", "funds", "reservedFunds",
                            "promocode", "enabled", "isCustomer", "registrationDate",
                            "lastVisit", "ordersLimit"})
    private User solver;

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

    public User getSolver() {
        return solver;
    }

    public void setSolver(User solver) {
        this.solver = solver;
    }

    public Long getSuggestionId() {
        return suggestionId;
    }

    public void setSuggestionId(Long suggestionId) {
        this.suggestionId = suggestionId;
    }

    @Override
    public String getText() {
        return "Исполнитель предложил решить Ваш заказ за " + price + " рублей";
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

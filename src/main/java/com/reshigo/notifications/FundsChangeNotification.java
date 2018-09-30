package com.reshigo.notifications;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Created by dmitry103 on 06/06/17.
 */
public class FundsChangeNotification extends Notification {

    public FundsChangeNotification(Long paymentId, Double amount, Double funds) {

        setAmount(amount);
        setFunds(funds);
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private NotificationTypeEnum type = NotificationTypeEnum.FUNDS;

    private Long paymentId;

    private Double amount;

    private Double funds;

    private boolean show = true;

    public NotificationTypeEnum getType() {
        return type;
    }

    @JsonIgnore
    @Override
    public SimpleNotificationData getPushData() {
        return new SimpleNotificationData(paymentId, type);
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
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
        if (amount > 0) {
            return "Ваш балланс пополнен на " + amount + " рублей.";
        } else {
            return "С Вашего счета удержаны средства в размере " + (-1) * amount + " рублей.";
        }
    }

    public Double getFunds() {
        return funds;
    }

    public void setFunds(Double funds) {
        this.funds = funds;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }
}

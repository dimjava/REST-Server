package com.reshigo.notifications;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reshigo.model.entity.permanent.OrderStatusEnum;

import java.util.concurrent.ThreadLocalRandom;

import static com.reshigo.model.entity.permanent.OrderStatusEnum.AVAILABLE;

/**
 * Created by dmitry103 on 06/06/17.
 */
public class OrderStatusChangeNotification extends Notification {

    public OrderStatusChangeNotification(Long id, OrderStatusEnum status) {
        setId(id);
        setStatus(status);
    }

    public OrderStatusChangeNotification(Long id, OrderStatusEnum status, boolean forCustomer) {
        this.forCustomer = forCustomer;
        setId(id);
        setStatus(status);
    }

    @JsonIgnore
    private boolean forCustomer = true;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private NotificationTypeEnum type = NotificationTypeEnum.ORDER_STATUS;

    private Long id;

    private boolean show = true;

    @JsonIgnore
    private String additionalForText = "";

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private OrderStatusEnum status;

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

    public OrderStatusEnum getStatus() {
        return status;
    }

    public void setStatus(OrderStatusEnum status) {
        this.status = status;
    }

    public NotificationTypeEnum getType() {
        return type;
    }

    public String getText() {
        switch (getStatus()) {
            case EXPIRED:
                return "К сожалению, никто не взялся за Ваш заказ.";
            case AVAILABLE:
                if (forCustomer) {
                    return "Ваш заказ снова доступен для исполнителей";
                } else {
                    return "Доступен новый заказ" + additionalForText;
                }
            case MODERATION:
                return "Заказ отправлен на проверку модератору";
            case DONE:
                return "Заказчик согласился с решением заказа";
            case DONE_MODER:
                if (forCustomer) {
                    return "Модератор подтвердил решение. Средства будут переведены исполнителю.";
                } else {
                    return "Модератор решил спор в Вашу пользу";
                }
            case DONE_SYS:
                if (forCustomer) {
                    return "Ввиду Вашего бездействия, средства автоматически переведены исполнителю";
                } else {
                    return "Средства за заказ переведены автоматически, ввиду бездействия заказчика";
                }
            case REJECTED:
                return "Заказчик не согласен с решением";
            case REJECTED_MODER:
                if (forCustomer) {
                    return "Модератор отклонил решение. Средства за заказ остаются у Вас";
                } else {
                    return "Модератор отклонил решение. Средства возвращены заказчику";
                }
            case REJECTED_SYS:
                if (forCustomer) {
                    return "Исполнитель не подал апелляцию, поэтому Вам возвращены средства за заказ";
                } else {
                    return "Ввиду Вашего бездействия, заказ автоматически считается нерешенным";
                }
            case RESERVED:
                if (forCustomer) {
                    return "Ваш заказ зарезервирован";
                } else {
                    return null;
                }
            case SOLVED:
                return "Ваш заказ решен";
            case UNACCEPTABLE:
                return "Ваш заказ удален из системы модератором. Для получения подробностей обратитесь в поддержку";
            default:
                return null;
        }
    }

    public void setShow(boolean b) {
        this.show = b;
    }

    public boolean getShow() {
        return this.show;
    }

    @Override
    public String getSound() {
        if (getStatus().equals(AVAILABLE)) {
            if (ThreadLocalRandom.current().nextFloat() > 0.1) {
                return "";
            }
        }

        return "default";
    }

    public String convert() throws Exception {
        return new ObjectMapper().writeValueAsString(this);
    }

    public void setAdditionalForText(String additionalForText) {
        this.additionalForText = additionalForText;
    }
}

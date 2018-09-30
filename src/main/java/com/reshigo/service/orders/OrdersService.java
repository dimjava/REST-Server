package com.reshigo.service.orders;

import com.reshigo.dao.OrderDao;
import com.reshigo.dao.PictureDao;
import com.reshigo.dao.UserDao;
import com.reshigo.exception.NotAllowed;
import com.reshigo.exception.NotAvailable;
import com.reshigo.exception.NotFound;
import com.reshigo.model.entity.Order;
import com.reshigo.model.entity.Picture;
import com.reshigo.model.entity.User;
import com.reshigo.notifications.*;
import org.apache.commons.lang.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static com.reshigo.model.entity.permanent.ChatStatusEnum.OPEN;
import static com.reshigo.model.entity.permanent.OrderStatusEnum.AVAILABLE;
import static com.reshigo.model.entity.permanent.OrderStatusEnum.RESERVED;
import static com.reshigo.notifications.NotificationTopicEnum.*;

/**
 * Created by dmitry103 on 16/07/16.
 */

@Service
public class OrdersService {

    private Logger logger = LoggerFactory.getLogger(OrdersService.class);

    @Autowired @Qualifier("feedService")
    private NotificationMessagingTemplate notificationMessagingTemplate;

    @Autowired
    private UserDao userDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private PictureDao pictureDao;

    @Autowired
    private OrdersServiceHelper helper;

    @Transactional
    public List<Order> getAvailableOrders(String solvername, Long offsetDate, Long offset, Long limit, Long subjectId) {
        //get orders with maturity date later than 5 minutes on.
        return orderDao.get(solvername, false, offset, limit, new Timestamp(offsetDate), subjectId, new Date().getTime(), AVAILABLE);
    }

    @Transactional
    public Order getAvailableOrder(Long id) throws NotFound {
        Order order = orderDao.findOne(id);

        if (order == null || !order.getStatus().equals(AVAILABLE)) {
            throw new NotFound(null);
        }

        return order;
    }

    public void reserve(Long orderId, String solvername) throws NotAllowed, NotAvailable, NotFound {
        Order order = helper.reserve(orderId, solvername);

        OrderStatusChangeNotification n = new OrderStatusChangeNotification(orderId, RESERVED);
        OrderStatusChangeNotification ns = new OrderStatusChangeNotification(orderId, RESERVED, false);
        notificationMessagingTemplate.send(n, order.getUser());

        ns.setShow(false);
        notificationMessagingTemplate.echo(ns, order.getSolver());
        notificationMessagingTemplate.send(ns, SOLVER.name(), solvername);

        ChatStatusChangeNotification ch = new ChatStatusChangeNotification(order.getChat().getId(), OPEN);
        ch.setShow(false);
        notificationMessagingTemplate.send(ch, order.getUser());
        notificationMessagingTemplate.send(ch, order.getSolver());
    }

    @Transactional
    public Picture getOrderPicture(Long orderId, Long pictureId) throws NotFound {
        Picture picture = pictureDao.findOne(pictureId);

        if (picture == null) {
            throw new NotFound(null);
        }

        if (!picture.getOrder().getId().equals(orderId)) {
            throw new NotFound(null);
        }

        return picture;
    }

    @Transactional
    public byte[] getOrderPictureAsFile(Long orderId, Long pictureId) throws NotFound {
        Picture picture = pictureDao.findOne(pictureId);

        if (picture == null) {
            throw new NotFound(null);
        }

        if (!picture.getOrder().getId().equals(orderId)) {
            throw new NotFound(null);
        }

        return picture.getImg().getData();
    }

    @Transactional
    public List<Picture> getOrderPicturesIds(Long orderId) throws NotFound, NotAllowed {
        Order order = orderDao.findOne(orderId);

        if (order == null) {
            throw new NotFound(null);
        }

        if (!order.getStatus().equals(AVAILABLE)) {
            throw new NotAllowed(null);
        }

        return pictureDao.getIdsAndCounters(orderId);
    }

    @Transactional
    public void suggestPrice(Long orderId, Long price, String comment, String name) throws NotFound, NotAvailable, NotAllowed, Exception {
        MutableLong suggestionId = new MutableLong(-1);
        Order order = helper.suggestPrice(orderId, price, comment, name, suggestionId);

        User solver = userDao.findOne(name);

        PriceSuggestNotification notification = new PriceSuggestNotification();
        notification.setPrice(price);
        notification.setOrderId(orderId);
        notification.setSolver(solver);
        notification.setSuggestionId(suggestionId.longValue());
        notification.setComment(comment);

        notificationMessagingTemplate.send(notification, order.getUser());

        PriceSuggestNotification notification1 = null;
        try {
            notification1 = Notification.clone(notification);
            notification1.setShow(false);
        } catch (CloneNotSupportedException e) {
            logger.error("Unable to clone notification", e);
        }

        notificationMessagingTemplate.echo(notification1, solver);
    }
}

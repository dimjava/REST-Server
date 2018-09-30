package com.reshigo.service;

import com.microsoft.azure.storage.StorageException;
import com.reshigo.dao.*;
import com.reshigo.dao.permanent.ThemesDao;
import com.reshigo.exception.*;
import com.reshigo.model.entity.*;
import com.reshigo.model.entity.permanent.OrderStatusEnum;
import com.reshigo.notifications.*;
import com.reshigo.service.scheduled.ScheduledTasks;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.reshigo.model.entity.permanent.ChatStatusEnum.CLOSED;
import static com.reshigo.model.entity.permanent.ChatStatusEnum.OPEN;
import static com.reshigo.model.entity.permanent.OrderStatusEnum.*;
import static com.reshigo.notifications.NotificationTopicEnum.SOLVER;
import static javax.management.timer.Timer.ONE_DAY;

@Service
public class UserOrdersService {

    private Logger logger = LoggerFactory.getLogger(UserOrdersService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private PictureDao pictureDao;

    @Autowired
    private ThemesDao themesDao;

    @Autowired
    private FeedDao feedDao;

    @Autowired @Qualifier("feedService")
    private NotificationMessagingTemplate notificationMessagingTemplate;

    @Autowired
    private ScheduledTasks scheduler;

    @Autowired
    private UserOrdersServiceHelper helper;

    @Autowired
    private PriceSuggestDao priceSuggestDao;

    @Transactional
    public List<Order> getAllOrders(Long offset, Long limit, String status, String username) {
        User user = userDao.findOne(username);

        if (status.contains(",")) {
            List<OrderStatusEnum> statuses = new LinkedList<>();

            for (String s : status.split(",")) {
                statuses.add(valueOf(s));
            }

            return orderDao.get(username, user.getIsCustomer(), offset, limit, null, -1L, null,
                    statuses.toArray(new OrderStatusEnum[statuses.size()]));
        }

        switch (status) {
            case "ACTIVE":
                return orderDao.get(username, user.getIsCustomer(), offset, limit, null, -1L, null,
                        AVAILABLE,
                        RESERVED,
                        MODERATION,
                        SOLVED,
                        REJECTED, //TODO: think about it, man
                        REJECTED_APPEAL);
            case "NON_ACTIVE":
                return orderDao.get(username, user.getIsCustomer(), offset, limit, null, -1L, null,
                        DONE,
                        DONE_SYS,
                        DONE_MODER,
                        EXPIRED,
                        UNACCEPTABLE,
                        REJECTED_CONF,
                        REJECTED_SYS,
                        REJECTED_MODER);
            default:
                return orderDao.get(username, user.getIsCustomer(), offset, limit, null, -1L, null, valueOf(status));
        }

    }

    @Transactional
    public Order getOrder(Long orderId) throws NotFound, NotAvailable {
        Order order = orderDao.findOne(orderId);

        if (order == null) {
            throw new NotFound(null);
        }

        Hibernate.initialize(order.getPictures());

        return order;
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

    public Order sendOrderToAvailable(Order order) throws NotFound, NotAvailable, NotAllowed, ParamsError {
        Order ret = helper.sendOrderToAvailable(order);

        scheduler.scheduleOrderMaturity(order.getId(), order.getMaturityDate());

        OrderStatusChangeNotification notification = new OrderStatusChangeNotification(order.getId(), AVAILABLE, false);
        notification.setAdditionalForText(" за " + order.getPrice() + " рублей");
        notificationMessagingTemplate.send(notification, SOLVER.name());


        OrderStatusChangeNotification notification1 = new OrderStatusChangeNotification(order.getId(), AVAILABLE, false);
        notification1.setShow(false);
        notificationMessagingTemplate.echo(notification1, ret.getUser());

        return ret;
    }

    @Transactional(rollbackFor = IOException.class)
    public Picture addOrderPicture(Picture picture, Long orderId) throws NotFound, NotAvailable, IOException, URISyntaxException, StorageException {
        Order order = orderDao.findOne(orderId);

        if (order == null ) {
            throw new NotFound(null);
        }

        if (!order.getStatus().equals(DRAFT)) {
            throw new NotAvailable(null);
        }

        if (order.getPictures().size() == 3) {
            throw new NotAvailable(null);
        }

        picture.setOrder(order);
        pictureDao.save(picture);

        return picture;
    }

    @Transactional(rollbackFor = IOException.class)
    public Picture addOrderPicture(Long orderId, Long counter,  byte[] data) throws NotFound, NotAvailable, IOException, URISyntaxException, StorageException {
        if (counter == null) {
            throw new NotAvailable(null);
        }

        Order order = orderDao.findOne(orderId);

        if (order == null ) {
            throw new NotFound(null);
        }

        if (!order.getStatus().equals(DRAFT)) {
            throw new NotAvailable(null);
        }

        if (order.getPictures().size() == 3) {
            throw new NotAvailable(null);
        }

        PictureData pd = new PictureData();
        pd.setData(data);
        Picture picture = new Picture();
        picture.setCounter(counter);
        picture.setOrder(order);
        picture.setImg(pd);

        pictureDao.save(picture);

        return picture;
    }

    @Transactional
    public void deleteOrder(Long orderId) throws NotFound, NotAllowed {
        User user = helper.deleteOrder(orderId);

        OrderStatusChangeNotification notification1 = new OrderStatusChangeNotification(orderId, DELETED, true);
        notification1.setShow(false);
        notificationMessagingTemplate.send(notification1, user);

        OrderStatusChangeNotification notification2 = new OrderStatusChangeNotification(orderId, DELETED, false);
        notification2.setShow(false);
        notificationMessagingTemplate.send(notification2, SOLVER.name());
    }

    @Transactional
    public void deleteOrderPicture(Long orderId, Long pictureId) throws NotFound, NotAllowed {
        Order order = orderDao.findOne(orderId);

        if (order == null) {
            throw new NotFound(null);
        }

        if (!order.getStatus().equals(DRAFT)) {
            throw new NotAllowed(null);
        }

        for (Picture picture : order.getPictures()) {
            if (picture.getId().equals(pictureId)) {
                order.getPictures().remove(picture);
                pictureDao.delete(picture);

                return;
            }
        }

        throw new NotFound(null);
    }

    public void updateMaturityDate(Long orderId, Long diff) throws NotAllowed, NotFound {
        Order order = helper.updateMaturityDate(orderId, diff);

        OrderMaturityChangeNotification notification = new OrderMaturityChangeNotification(orderId, order.getMaturityDate().getTime());
        notificationMessagingTemplate.send(notification, order.getSolver());

        OrderMaturityChangeNotification notification1 = new OrderMaturityChangeNotification(orderId, order.getMaturityDate().getTime());
        notification1.setShow(false);
        notificationMessagingTemplate.echo(notification1, order.getUser());
    }

    public void closeOrderSolved(Long orderId) throws NotAvailable, NotFound {
        Order order = helper.closeOrderSolved(orderId);

        OrderStatusChangeNotification notification = new OrderStatusChangeNotification(orderId, SOLVED);
        notificationMessagingTemplate.send(notification, order.getUser());

        OrderStatusChangeNotification notification1 = new OrderStatusChangeNotification(orderId, SOLVED);
        notification1.setShow(false);
        notificationMessagingTemplate.echo(notification1, order.getSolver());
    }

    public void rejectOrder(Long orderId) throws NotAvailable, NotFound {
        Order order = helper.rejectOrder(orderId);

        //schedule automatic rejection
        Date date = new Date(new Date().getTime() + ONE_DAY);
        scheduler.scheduleOrderMaturity(orderId, date);

        OrderStatusChangeNotification notification = new OrderStatusChangeNotification(orderId, REJECTED, false);
        notificationMessagingTemplate.send(notification, order.getSolver());

        OrderStatusChangeNotification notification1 = new OrderStatusChangeNotification(orderId, REJECTED, false);
        notification1.setShow(false);
        notificationMessagingTemplate.echo(notification1, order.getUser());

        ChatStatusChangeNotification ch = new ChatStatusChangeNotification(order.getChat().getId(), CLOSED);
        notificationMessagingTemplate.send(ch, order.getUser());
        notificationMessagingTemplate.send(ch, order.getSolver());
    }

    public void closeOrderDone(Long orderId) throws NotAvailable, NotFound {
        Order order = helper.closeOrderDone(orderId);

        OrderStatusChangeNotification notification = new OrderStatusChangeNotification(orderId, DONE, false);
        notificationMessagingTemplate.send(notification, order.getSolver());

        OrderStatusChangeNotification notification1 = new OrderStatusChangeNotification(orderId, DONE, false);
        notification1.setShow(false);
        notificationMessagingTemplate.echo(notification1, order.getUser());
    }


    public void appeal(Long orderId) throws NotFound, NotAvailable {
        Order order = helper.appeal(orderId);

        OrderStatusChangeNotification notification = new OrderStatusChangeNotification(orderId, REJECTED_APPEAL);
        notification.setShow(false);
        notificationMessagingTemplate.echo(notification, order.getSolver());
        notificationMessagingTemplate.send(notification, order.getUser());

        ChatStatusChangeNotification ch = new ChatStatusChangeNotification(order.getChat().getId(), CLOSED);
        notificationMessagingTemplate.send(ch, order.getUser());
        notificationMessagingTemplate.send(ch, order.getSolver());
    }

    @Transactional
    public void confirmRejected(Long orderId) throws NotFound, NotAvailable {
        Order order = orderDao.findOne(orderId);

        if (order == null) {
            throw new NotFound(null);
        }

        if (!order.getStatus().equals(REJECTED)) {
            throw new NotAvailable(null);
        }

        order.setStatus(REJECTED_CONF);

        //release user's funds
        User user = userDao.getUserWithLock(order.getUser().getName());
        BigDecimal af = new BigDecimal(order.getPrice());
        user.setReservedFunds(user.getReservedFunds().subtract(af));

        OrderStatusChangeNotification notification = new OrderStatusChangeNotification(orderId, REJECTED_CONF);
        notification.setShow(false);
        notificationMessagingTemplate.echo(notification, order.getSolver());
        notificationMessagingTemplate.send(notification, order.getUser());
    }

    public void confirmPriceSuggest(Long orderId, Long suggestId) throws NotAllowed, NotFound, NotAvailable, ParamsError, FundsError {
        Order order = helper.confirmPriceSuggest(orderId, suggestId);

        OrderStatusChangeNotification statusChangeNotification = new OrderStatusChangeNotification(orderId, RESERVED);
        PriceConfirmNotification priceConfirmNotification = new PriceConfirmNotification(orderId, order.getPrice(), suggestId);
        ChatStatusChangeNotification chatStatusChangeNotification = new ChatStatusChangeNotification(order.getChat().getId(), OPEN);
        PriceUpdateNotification priceUpdateNotification = new PriceUpdateNotification(orderId, order.getPrice(), false);

        notificationMessagingTemplate.send(priceConfirmNotification, order.getSolver());
        notificationMessagingTemplate.send(statusChangeNotification, order.getUser());
        notificationMessagingTemplate.send(chatStatusChangeNotification, order.getUser());
        notificationMessagingTemplate.send(chatStatusChangeNotification, order.getSolver());

        notificationMessagingTemplate.echo(priceUpdateNotification, order.getUser());
        notificationMessagingTemplate.echo(priceUpdateNotification, order.getSolver());
        notificationMessagingTemplate.echo(priceConfirmNotification, order.getSolver());

        OrderStatusChangeNotification statusChangeNotificationS = new OrderStatusChangeNotification(orderId, RESERVED, false);
        statusChangeNotificationS.setShow(false);
        notificationMessagingTemplate.echo(statusChangeNotificationS, order.getSolver());
        notificationMessagingTemplate.send(statusChangeNotificationS, SOLVER.name(), order.getSolver().getName());

        PriceConfirmNotification priceConfirmNotification1 = new PriceConfirmNotification(orderId, order.getPrice(), suggestId);
        priceConfirmNotification1.setShow(false);
        notificationMessagingTemplate.echo(priceConfirmNotification1, order.getUser());

        OrderStatusChangeNotification statusChangeNotification1 = new OrderStatusChangeNotification(orderId, RESERVED);
        statusChangeNotification1.setShow(false);
        notificationMessagingTemplate.echo(statusChangeNotification1, order.getUser());
    }

    @Transactional
    public List<PriceSuggest> getSuggestions(Long id) {
        return priceSuggestDao.getSuggestions(id);
    }

    @Transactional
    public void addReview(Long orderId, Review review) throws NotFound, NotAllowed {
        Order order = orderDao.findOne(orderId);

        if (order == null) {
            throw new NotFound(null);
        }

        if (order.getReview() != null) {
            throw new NotAllowed("Нельзя оценить заказ второй раз");
        }

        order.setReview(review);
        orderDao.update(order);

        helper.recomputeRating(order.getSolver().getName());
    }
}

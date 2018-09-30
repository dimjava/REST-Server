package com.reshigo.service.moderator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reshigo.dao.FeedDao;
import com.reshigo.dao.OrderDao;
import com.reshigo.dao.PaymentDao;
import com.reshigo.dao.UserDao;
import com.reshigo.exception.NotAllowed;
import com.reshigo.exception.NotFound;
import com.reshigo.model.entity.Feed;
import com.reshigo.model.entity.Order;
import com.reshigo.model.entity.Payment;
import com.reshigo.model.entity.User;
import com.reshigo.model.entity.permanent.OrderStatusEnum;
import com.reshigo.model.moderator.Users2Notify;
import com.reshigo.notifications.*;
import com.reshigo.service.ServiceHelper;
import com.reshigo.service.UserOrdersServiceHelper;
import io.jsonwebtoken.lang.Collections;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.method.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static com.reshigo.model.entity.permanent.OrderStatusEnum.*;
import static java.math.BigDecimal.*;
import static java.math.BigDecimal.valueOf;

/**
 * Created by dmitry103 on 27/12/16.
 */

@Service
public class ModeratorService {
    Logger logger = LoggerFactory.getLogger(ModeratorService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private FeedDao feedDao;

    @Autowired
    private ServiceHelper serviceHelper;

    @Autowired
    private UserOrdersServiceHelper ordersServiceHelper;

    @Autowired @Qualifier("feedService")
    private NotificationMessagingTemplate notificationMessagingTemplate;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModeratorServiceHelper helper;

    @Transactional
    public List<User> getUsers(Long page, Long perPage, String nameFilter, String phoneFilter) {
        return userDao.getUsers(page, perPage, nameFilter, phoneFilter);
    }

    @Transactional
    public List<Payment> getPayments(Long page, Long perPage, String nameFilter) {
        return paymentDao.getPayments(true, page, perPage, nameFilter);
    }

    @Transactional
    public List<Order> getOrders(Long page, Long perPage, String nameFilter, String solverNameFilter, String status) {
        List<Order> orders = orderDao.orders(page, perPage, nameFilter, solverNameFilter, status);
        orders.forEach(o -> {
            if (o.getReview() != null) Hibernate.initialize(o.getReview());
        });

        return orders;
    }

    @Transactional
    public void deleteOrder(Long id) throws NotAllowed {
        Order order = orderDao.getWithLock(id);

        if (order == null) {
            return;
        }

        if (order.getStatus() == OrderStatusEnum.DONE || order.getStatus() == OrderStatusEnum.REJECTED
                || order.getStatus() == OrderStatusEnum.EXPIRED) {
            orderDao.delete(order);

            return;
        }

        throw new NotAllowed(null);
    }

    @Transactional
    public void blockUser(String name) {
        User user = userDao.getUserWithLock(name);

        user.setEnabled(!user.isEnabled());
    }

    //TODO: think about sending notifications after transaction commited changes
    @Transactional
    public void updateStatus(Long orderId, OrderStatusEnum status) throws NotFound, NotAllowed {
        Order order = orderDao.getWithLock(orderId);

        if (order == null) {
            throw new NotFound(null);
        }

        List<OrderStatusEnum> values = Collections.arrayToList(new OrderStatusEnum[]{
                REJECTED_APPEAL, REJECTED, AVAILABLE, MODERATION});

        if (!values.contains(order.getStatus())) {
            throw new NotAllowed(null);
        }

        if (order.getStatus().equals(REJECTED_APPEAL) || order.getStatus().equals(REJECTED)) {
            OrderStatusChangeNotification n = new OrderStatusChangeNotification(orderId, status);
            OrderStatusChangeNotification ns = new OrderStatusChangeNotification(orderId, status, false);

            if (status.equals(REJECTED_MODER)) {
                order.setStatus(status);

                //release user's funds
                User user = userDao.getUserWithLock(order.getUser().getName());
                BigDecimal af = new BigDecimal(order.getPrice());
                user.setReservedFunds(user.getReservedFunds().subtract(af));
            } else if (status.equals(DONE_MODER)) {
                order.setStatus(status);
                serviceHelper.transferFunds(order.getUser().getName(), order.getSolver().getName(),
                        valueOf(order.getPrice()));
            } else {
                throw new NotAllowed(null);
            }

            notificationMessagingTemplate.send(n, order.getUser());
            notificationMessagingTemplate.send(ns, order.getSolver());

            return;
        }

        if (order.getStatus().equals(AVAILABLE) && status.equals(MODERATION)) {
            order.setStatus(MODERATION);

            //release user's funds
            User user = userDao.getUserWithLock(order.getUser().getName());
            BigDecimal af = new BigDecimal(order.getPrice());
            user.setReservedFunds(user.getReservedFunds().subtract(af));

            Notification n = new OrderStatusChangeNotification(orderId, MODERATION);
            notificationMessagingTemplate.send(n, order.getUser());

            return;
        }

        if (order.getStatus().equals(MODERATION) && (status.equals(UNACCEPTABLE) || status.equals(AVAILABLE))) {
            order.setStatus(status);

            Notification n = new OrderStatusChangeNotification(orderId, status);
            notificationMessagingTemplate.send(n, order.getUser());

            return;
        }

        throw new NotAllowed(null);
    }

    @Transactional
    public List<Feed> getFeeds(Long page, Long perPage, String nameFilter) {
        return feedDao.getFeeds(page, perPage, nameFilter);
    }

    @Transactional
    public void createFeed(String template) throws IOException {
        Users2Notify t = om.readValue(template, Users2Notify.class);
        TextNotification n = new TextNotification(t.getTitle(), t.getMessage());

        for (String name : t.getNames().split(",|\\n")) {
            User u = userDao.findOne(name.trim());

            if (u == null) {
                if (!name.equals("") && !(name == null)) {
                    // then suppose it is a topic
                    notificationMessagingTemplate.send(n, name);
                }
            } else {
                notificationMessagingTemplate.send(n, u);
            }
        }
    }

    @Transactional
    public void releaseFunds(String solverName, Double amount, String commission, String comment) throws NotFound, NotAllowed, Exception {
        User user = userDao.getUserWithLock(solverName);

        if (user == null) {
            throw new NotFound("User not found");
        }

        BigDecimal fd = new BigDecimal(amount);

        if (amount < 0 && user.getFunds().compareTo(fd.abs()) < 0) {
            throw new NotAllowed("Not enough funds");
        }

        Payment payment = new Payment();
        payment.setAmount(fd);

        if (commission == null) {
            payment.setCommission(user.getCommission());
        } else {
            if (Double.parseDouble(commission) < 0 || Double.parseDouble(commission) > 1) {
                throw new NotAllowed("Commission is not correct");
            }

            payment.setCommission(new BigDecimal(commission));
        }

        if (amount < 0) {
            payment.setBonusAmount(user.getBonusFunds().min(fd.abs()).multiply(valueOf(fd.signum())));
        } else {
            payment.setBonusAmount(fd);
            payment.setCommission(ZERO);
        }

        payment.setCompleted(true);
        payment.setDate(new Timestamp(new Date().getTime()));
        payment.setId(0L);
        payment.setUser(user);
        payment.setComment(comment);
        paymentDao.save(payment);

        user.setFunds(user.getFunds().add(fd));
        user.setBonusFunds(user.getBonusFunds().add(payment.getBonusAmount()));

        FundsChangeNotification fundsChangeNotification = new FundsChangeNotification(payment.getId(), amount, user.getFunds().doubleValue());
        notificationMessagingTemplate.send(fundsChangeNotification, user);
    }

    @Transactional
    public void actOnReview(Long orderId) {
        helper.actOnReview(orderId);

        Order order = orderDao.findOne(orderId);

        if (order != null) {
            ordersServiceHelper.recomputeRating(order.getSolver().getName());
        }
    }

    @Transactional
    public BigDecimal[] getFundsStatistics(Date from, Date until) {
        BigDecimal receivedBon = ZERO;
        BigDecimal receivedReal = ZERO;
        BigDecimal payedBonNoCom = ZERO;
        BigDecimal payedRealNoCom = ZERO;
        BigDecimal payedBonCom = ZERO;
        BigDecimal payedRealCom = ZERO;

        for (Payment p : paymentDao.getPayments(from, until)) {
            if (p.getAmount().compareTo(ZERO) > 0) {
                receivedBon = receivedBon.add(p.getBonusAmount());
                receivedReal = receivedReal.add(p.getAmount().subtract(p.getBonusAmount()));
            } else {
                payedBonNoCom = payedBonNoCom.add(p.getBonusAmount());
                payedRealNoCom = payedRealNoCom.add(p.getAmount().subtract(p.getBonusAmount()));

                payedBonCom = payedBonCom.add(p.getBonusAmount().multiply(ONE.subtract(p.getCommission())));
                payedRealCom = payedRealCom.add(
                        (
                            p.getAmount().subtract(p.getBonusAmount())
                        ).multiply(ONE.subtract(p.getCommission()))
                );
            }
        }

        return new BigDecimal[]{receivedBon, receivedReal, payedBonNoCom, payedRealNoCom,
                payedBonCom, payedRealCom};
    }

    public List<String> fundsConsistencyCheck() {
        LinkedList<String> need2Fix = new LinkedList<>();

        List<Object[]> stats = userDao.getUserFundsStats();
        stats.addAll(userDao.getSolverFundsStats());

        stats.forEach(o -> {
                String name = (String) o[0];
                BigDecimal balance = (BigDecimal) o[1];

                if (balance.compareTo(BigDecimal.ZERO) != 0) {
                    logger.warn("Balance check failed. Name: {}, balance: {}", name, balance);
                    need2Fix.add(name);
                }
        });

        return need2Fix;
    }

    @Transactional
    public void updatePrice(Long id, Long price) throws NotFound, NotAllowed {
        if (price <= 0 || price > 10000) {
            throw new NotAllowed("Цена заказа не должна быть меньше 0 или больше 10000");
        }

        Order order = orderDao.findOne(id);

        if (order == null) {
            throw new NotFound(null);
        }

        if (order.getStatus() != RESERVED && order.getStatus() != SOLVED) {
            throw new NotAllowed("Заказ должен быть RESERVED или SOLVED");
        }

        if (price.equals(order.getPrice())) {
            return;
        }

        User user = userDao.getUserWithLock(order.getUser().getName());
        BigDecimal diff = new BigDecimal(price - order.getPrice());

        if (price > order.getPrice()) {
            if (user.getFunds().subtract(user.getReservedFunds()).compareTo(diff) < 0) {
                throw new NotAllowed("У заказчика недостаточно средств на счету");
            }
        }

        user.setReservedFunds(user.getReservedFunds().add(diff));
        order.setPrice(price);

        userDao.update(user);
        orderDao.update(order);

        PriceUpdateNotification priceUpdateNotification = new PriceUpdateNotification(id, order.getPrice(), true);
        notificationMessagingTemplate.echo(priceUpdateNotification, order.getUser());
        notificationMessagingTemplate.echo(priceUpdateNotification, order.getSolver());
    }
}

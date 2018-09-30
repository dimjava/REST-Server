package com.reshigo.service.scheduled;

import com.reshigo.controller.orders.OrdersMaturityTaskMaster;
import com.reshigo.dao.OrderDao;
import com.reshigo.dao.UserDao;
import com.reshigo.model.entity.Chat;
import com.reshigo.model.entity.Order;
import com.reshigo.model.entity.User;
import com.reshigo.model.entity.permanent.OrderStatusEnum;
import com.reshigo.notifications.ChatStatusChangeNotification;
import com.reshigo.notifications.Notification;
import com.reshigo.notifications.NotificationMessagingTemplate;
import com.reshigo.notifications.OrderStatusChangeNotification;
import com.reshigo.service.ServiceHelper;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import static com.reshigo.model.entity.permanent.ChatStatusEnum.CLOSED;
import static com.reshigo.model.entity.permanent.ChatStatusEnum.OPEN;
import static com.reshigo.model.entity.permanent.OrderStatusEnum.*;
import static javax.management.timer.Timer.ONE_DAY;
import static javax.management.timer.Timer.ONE_HOUR;

/**
 * Created by dmitry103 on 11/12/16.
 */

@Service
@Scope("singleton")
public class ScheduledTasksImpl implements ScheduledTasks {

    private Logger logger = LoggerFactory.getLogger(ScheduledTasksImpl.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private SessionFactory sessionFactory;

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;

    @Autowired
    private UserDao userDao;

    @Autowired
    private OrderDao orderDao;

    private ServiceHelper serviceHelper;

    @Autowired @Qualifier("feedService")
    private NotificationMessagingTemplate notificationMessagingTemplate;

    private Boolean isInitialized = Boolean.FALSE;

    @Autowired
    public ScheduledTasksImpl(ThreadPoolTaskScheduler threadPoolTaskScheduler) {

        logger.debug("ScheduledTasks constructor proceed");

        this.threadPoolTaskScheduler = threadPoolTaskScheduler;
    }

    public void scheduleOrderMaturity(Long orderId, Date date) {
        OrdersMaturityTaskMaster taskMaster = applicationContext.getBean(OrdersMaturityTaskMaster.class);
        taskMaster.setId(orderId);

        threadPoolTaskScheduler.schedule(taskMaster, date);
    }

    @Transactional
    @Async
    @Scheduled(fixedDelay = 6 * ONE_HOUR)
    public void cleanFeedList() {
        Timestamp latest = new Timestamp(new Date().getTime() - 6 * ONE_HOUR);

        sessionFactory.getCurrentSession().createQuery("delete Feed where date < :latest")
                .setParameter("latest", latest).executeUpdate();
    }

    @Transactional
    @Async
    @Scheduled(fixedDelay = 1000 * ONE_DAY)
    public void init() {
        logger.debug("Proceed ScheduledTasks init");

        synchronized (isInitialized) {
            if (isInitialized) {
                return;
            }

            isInitialized = true;
        }

        this.userDao = new UserDao(sessionFactory);
        this.orderDao = new OrderDao(sessionFactory);
        this.serviceHelper = new ServiceHelper(this.userDao);

        List<Order> orders = sessionFactory.getCurrentSession().createCriteria(Order.class)
                .add(Restrictions.in("status", new OrderStatusEnum[]{AVAILABLE, SOLVED, RESERVED, REJECTED}))
                .list();

        for (Order order : orders) {
            scheduleOrderMaturity(order.getId(), order.getMaturityDate());
        }

        List<Chat> chats = sessionFactory.getCurrentSession().createCriteria(Chat.class)
                .add(Restrictions.eq("status", OPEN))
                .list();

        for (Chat chat : chats) {
            scheduleOrderMaturity(chat.getOrder().getId(), chat.getOrder().getMaturityDate());
        }
    }

    @Transactional
    public void cleanup(Long id) {
        OrdersMaturityTaskMaster taskMaster = applicationContext.getBean(OrdersMaturityTaskMaster.class);
        taskMaster.setId(id);

        Order order = orderDao.getWithLock(id);

        if (order == null) {
            return;
        }

        if (order.getMaturityDate().after(new Date())) {
            threadPoolTaskScheduler.schedule(taskMaster, order.getMaturityDate());

            return;
        }

        if (order.getChat().getStatus().equals(OPEN)) {
            if (new Date().getTime() - order.getMaturityDate().getTime() >= ONE_DAY) {
                order.getChat().setStatus(CLOSED);
                Notification notification = new ChatStatusChangeNotification(order.getChat().getId(), CLOSED);
                notificationMessagingTemplate.send(notification, order.getUser());
                notificationMessagingTemplate.send(notification, order.getSolver());
            } else {
                Date nextDate = new Date(order.getMaturityDate().getTime() + ONE_DAY);
                threadPoolTaskScheduler.schedule(taskMaster, nextDate);
            }
        }

        if (order.getStatus().equals(AVAILABLE)) {
            User user = userDao.getUserWithLock(order.getUser().getName());
            user.setReservedFunds(user.getReservedFunds().subtract(BigDecimal.valueOf(order.getPrice())));
            order.setStatus(EXPIRED);

            notificationMessagingTemplate.send(new OrderStatusChangeNotification(id, EXPIRED), order.getUser());

            return;
        }

        if (order.getStatus().equals(RESERVED)) {
            if (new Date().getTime() - order.getMaturityDate().getTime() >= ONE_HOUR) {
                User user = userDao.getUserWithLock(order.getUser().getName());
                user.setReservedFunds(user.getReservedFunds().subtract(BigDecimal.valueOf(order.getPrice())));
                order.setStatus(EXPIRED);
                order.getChat().setStatus(CLOSED);

                notificationMessagingTemplate.send(new OrderStatusChangeNotification(id, EXPIRED), user);

                ChatStatusChangeNotification ch = new ChatStatusChangeNotification(order.getChat().getId(), CLOSED);
                ch.setShow(false);
                notificationMessagingTemplate.echo(ch, order.getUser());
                notificationMessagingTemplate.echo(ch, order.getSolver());
            } else {
                Date nextDate = new Date(order.getMaturityDate().getTime() + ONE_HOUR);
                threadPoolTaskScheduler.schedule(taskMaster, nextDate);
            }

            return;
        }

        if (order.getStatus().equals(SOLVED)) {
            if (new Date().getTime() - order.getMaturityDate().getTime() >= ONE_DAY) {

                BigDecimal pv = BigDecimal.valueOf(order.getPrice());
                serviceHelper.transferFunds(order.getUser().getName(), order.getSolver().getName(), pv);

                order.setStatus(DONE_SYS);
                order.getChat().setStatus(CLOSED);

                notificationMessagingTemplate.send(new OrderStatusChangeNotification(id, DONE_SYS), order.getUser());
                notificationMessagingTemplate.send(new OrderStatusChangeNotification(id, DONE_SYS, false), order.getSolver());

                ChatStatusChangeNotification ch = new ChatStatusChangeNotification(order.getChat().getId(), CLOSED);
                ch.setShow(false);
                notificationMessagingTemplate.echo(ch, order.getUser());
                notificationMessagingTemplate.echo(ch, order.getSolver());
            } else {
                Date nextDate = new Date(order.getMaturityDate().getTime() + ONE_DAY);
                threadPoolTaskScheduler.schedule(taskMaster, nextDate);
            }

            return;
        }

        if (order.getStatus().equals(REJECTED)) {
            if (new Date().getTime() - order.getMaturityDate().getTime() >= ONE_DAY) {
                User user = userDao.getUserWithLock(order.getUser().getName());
                user.setReservedFunds(user.getReservedFunds().subtract(BigDecimal.valueOf(order.getPrice())));

                order.setStatus(REJECTED_SYS);

                notificationMessagingTemplate.send(new OrderStatusChangeNotification(id, REJECTED_SYS), order.getUser());
                notificationMessagingTemplate.send(new OrderStatusChangeNotification(id, REJECTED_SYS, false), order.getSolver());
            } else {
                Date nextDate = new Date(order.getMaturityDate().getTime() + ONE_DAY);
                threadPoolTaskScheduler.schedule(taskMaster, nextDate);
            }
        }
    }
}

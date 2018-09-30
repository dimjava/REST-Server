package com.reshigo.service.orders;

import com.reshigo.dao.OrderDao;
import com.reshigo.dao.PriceSuggestDao;
import com.reshigo.dao.UserDao;
import com.reshigo.exception.NotAllowed;
import com.reshigo.exception.NotAvailable;
import com.reshigo.exception.NotFound;
import com.reshigo.model.entity.Order;
import com.reshigo.model.entity.PriceSuggest;
import com.reshigo.model.entity.User;
import com.reshigo.service.chats.ChatsCheckerService;
import org.apache.commons.lang.mutable.MutableLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static com.reshigo.model.entity.permanent.ChatStatusEnum.OPEN;
import static com.reshigo.model.entity.permanent.OrderStatusEnum.AVAILABLE;
import static com.reshigo.model.entity.permanent.OrderStatusEnum.RESERVED;
import static javax.management.timer.Timer.ONE_MINUTE;

/**
 * Created by dmitry103 on 18/08/17.
 */

@Service
public class OrdersServiceHelper {

    @Autowired
    private UserDao userDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private PriceSuggestDao priceSuggestDao;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private ApplicationContext context;

    @Transactional
    public Order reserve(Long orderId, String solvername) throws NotAllowed, NotAvailable, NotFound {
        User solver = userDao.findOne(solvername);

        if (orderDao.get(solvername, false, 0L, 2L, null, -1L, null, RESERVED).size() >= solver.getOrdersLimit()) {
            throw new NotAllowed(null);
        }

        Order order = orderDao.getWithLock(orderId);

        if (order == null) {
            throw new NotFound(null);
        }

        if (order.getPrice() == 0L) {
            throw new NotAllowed(null);
        }

        if (order.getStatus().equals(AVAILABLE)) {
            order.setSolver(solver);
            order.setStatus(RESERVED);
            order.getChat().setStatus(OPEN);

            order.getChat().getParticipants().add(solver);
        } else {
            throw new NotAvailable(null);
        }

        orderDao.update(order);

        //schedule chat check
        ChatsCheckerService chs = context.getBean(ChatsCheckerService.class);
        chs.setId(order.getChat().getId());
        taskScheduler.schedule(chs, new Date(new Date().getTime() + ONE_MINUTE * 10));

        return order;
    }

    @Transactional
    public Order suggestPrice(Long orderId, Long price, String comment, String name, MutableLong suggestionId) throws NotFound, NotAvailable, NotAllowed, Exception {
        Order order = orderDao.findOne(orderId);

        if (order == null) {
            throw new NotFound(null);
        }

        if (!order.getStatus().equals(AVAILABLE)) {
            throw new NotAvailable(null);
        }

        if (price < 80) {
            throw new NotAllowed(null);
        }

        PriceSuggest ps = new PriceSuggest();
        ps.setId(0L);
        ps.setOrder(order);
        ps.setPrice(price);
        ps.setComment(comment);
        ps.setSolver(userDao.findOne(name));

        priceSuggestDao.save(ps);

        suggestionId.setValue(ps.getId());

        return order;
    }
}

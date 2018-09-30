package com.reshigo.service;

import com.reshigo.dao.OrderDao;
import com.reshigo.dao.PriceSuggestDao;
import com.reshigo.dao.UserDao;
import com.reshigo.dao.permanent.ThemesDao;
import com.reshigo.exception.*;
import com.reshigo.model.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.reshigo.model.entity.permanent.ChatStatusEnum.CLOSED;
import static com.reshigo.model.entity.permanent.ChatStatusEnum.OPEN;
import static com.reshigo.model.entity.permanent.OrderStatusEnum.*;

/**
 * Created by dmitry103 on 18/08/17.
 */

@Service
public class UserOrdersServiceHelper {

    private Logger logger = LoggerFactory.getLogger(UserOrdersServiceHelper.class);

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private ThemesDao themesDao;

    @Autowired
    private PriceSuggestDao priceSuggestDao;

    @Autowired
    private ServiceHelper serviceHelper;

    @Transactional
    public Order sendOrderToAvailable(Order order) throws NotFound, NotAvailable, NotAllowed, ParamsError {
        Order foundOrder = orderDao.findOne(order.getId());

        if (foundOrder == null) {
            throw new NotFound(null);
        }

        if (order.getPrice() < 80L && order.getPrice() != 0L) {
            throw new NotAvailable("Цена должна быть не ниже 80 рублей, либо договорная");
        }

        if (!foundOrder.getStatus().equals(DRAFT)) {
            throw new NotAvailable(null);
        }

        List<Picture> pictures = orderDao.getPictures(foundOrder.getId());

        //no orders without pictures are allowed
        if (pictures.isEmpty()) {
            throw new NotAvailable("Добавьте фотографии к вашему заказу");
        }

        User user = userDao.getUserWithLock(foundOrder.getUser().getName());
        BigDecimal af = user.getFunds().subtract(user.getReservedFunds());

        if (af.compareTo(BigDecimal.valueOf(order.getPrice())) == -1) {
            throw new NotAllowed("Недостаточно средств на счету");
        }

        foundOrder.setComment(order.getComment());

        order.setMaturityDate(new Timestamp(Long.max(order.getMaturityDate().getTime(), new Date().getTime() + 30L * 60 * 1000)));
        foundOrder.setMaturityDate(order.getMaturityDate());

        foundOrder.setUser(foundOrder.getUser());
        foundOrder.setDate(new Timestamp(new Date().getTime()));

        Theme theme = themesDao.findOne(order.getTheme().getId());

        if (theme == null) {
            throw new ParamsError("Укажите тему заказа");
        }

        foundOrder.setTheme(theme);
        foundOrder.setPrice(order.getPrice());
        foundOrder.setTasksCnt(order.getTasksCnt());

        foundOrder.setStatus(AVAILABLE);

        orderDao.update(foundOrder);

        user.setReservedFunds(user.getReservedFunds().add(new BigDecimal(order.getPrice())));

        return foundOrder;
    }

    @Transactional
    public Order updateMaturityDate(Long orderId, Long diff) throws NotAllowed, NotFound {
        Order order = orderDao.getWithLock(orderId);

        if (order == null) {
            throw new NotFound(null);
        }

        if (diff <= 0 || order.getSolver() == null) {
            throw new NotAllowed(null);
        }

        if (!order.getStatus().equals(RESERVED)) {
            throw new NotAllowed(null);
        }

        order.setMaturityDate(new Timestamp(order.getMaturityDate().getTime() + diff));

        return order;
    }

    @Transactional
    public Order closeOrderSolved(Long orderId) throws NotAvailable, NotFound {
        Order order = orderDao.getWithLock(orderId);

        if (order == null) {
            throw new NotFound(null);
        }

        if (!order.getStatus().equals(RESERVED)) {
            throw new NotAvailable(null);
        }

        order.setStatus(SOLVED);
        orderDao.update(order);

        return order;
    }

    @Transactional
    public Order rejectOrder(Long orderId) throws NotAvailable, NotFound {
        Order order = orderDao.findOne(orderId);

        if (order == null) {
            throw new NotFound(null);
        }

        if (!order.getStatus().equals(SOLVED)) {
            throw new NotAvailable(null);
        }

        order.setStatus(REJECTED);
        order.getChat().setStatus(CLOSED);

        return order;
    }

    @Transactional
    public Order closeOrderDone(Long orderId) throws NotAvailable, NotFound {
        Order order = orderDao.findOne(orderId);

        if (order == null) {
            throw new NotFound(null);
        }

        if (!order.getStatus().equals(SOLVED) && !order.getStatus().equals(RESERVED)) {
            throw new NotAvailable(null);
        }

        order.setStatus(DONE);

        serviceHelper.transferFunds(order.getUser().getName(), order.getSolver().getName(),
                BigDecimal.valueOf(order.getPrice()));

        return order;
    }

    @Transactional
    public Order appeal(Long orderId) throws NotFound, NotAvailable {
        Order order = orderDao.findOne(orderId);

        if (order == null) {
            throw new NotFound(null);
        }

        if (!order.getStatus().equals(REJECTED)) {
            throw new NotAvailable(null);
        }

        order.setStatus(REJECTED_APPEAL);

        return order;
    }

    @Transactional
    public Order confirmPriceSuggest(Long orderId, Long suggestId) throws NotAllowed, NotFound, NotAvailable, ParamsError, FundsError {
        Order order = orderDao.getWithLock(orderId);

        if (order == null) {
            throw new NotFound("Заказ не найден. Если вы видите данное сообщение, обратитесь в поддержку");
        }

        Timestamp curTime = new Timestamp(new Date().getTime());
        if (!order.getStatus().equals(AVAILABLE)) {
            throw new NotAvailable("Заказ уже взяли");
        }

        if (order.getMaturityDate().before(curTime)) {
            throw new NotAvailable("Время заказа истекло");
        }

        PriceSuggest ps = priceSuggestDao.findOne(suggestId);

        if (ps == null) {
            throw new NotFound("Предложение цены не найдено. Если вы видите данное сообщение, обратитесь в поддержку");
        }

        if (!ps.getOrder().getId().equals(orderId)) {
            throw new ParamsError(null);
        }

        User user = userDao.getUserWithLock(order.getUser().getName());

        //check funds
        BigDecimal priceDiff = BigDecimal.valueOf(ps.getPrice() - order.getPrice());
        BigDecimal fundsDiff = user.getFunds().subtract(user.getReservedFunds());
        if (fundsDiff.compareTo(priceDiff) < 0) {
            throw new FundsError("Недостаточно средств на счету");
        }

        int curReserved = orderDao.get(ps.getSolver().getName(), false,
                0L, ps.getSolver().getOrdersLimit(), null,
                -1L, null, RESERVED).size();

        if (curReserved < ps.getSolver().getOrdersLimit()) {
            user.setReservedFunds(user.getReservedFunds().add(priceDiff));
            order.setStatus(RESERVED);
            order.setSolver(ps.getSolver());
            order.getChat().getParticipants().add(ps.getSolver());
            order.getChat().setStatus(OPEN);
            order.setPrice(ps.getPrice());

            ps.setAccepted(true);
            priceSuggestDao.update(ps);

            return order;
        } else {
            throw new NotAllowed("К сожалению, исполнитель в данный момент уже занят выполнением других заказов" +
                    " и не может взяться за ваш заказ");
        }
    }

    @Transactional
    public User deleteOrder(Long orderId) throws NotFound, NotAllowed {
        Order order = orderDao.getWithLock(orderId);

        if (order == null) {
            throw new NotFound(null);
        }

        if (!order.getStatus().equals(AVAILABLE)) {
            throw new NotAllowed(null);
        }

        orderDao.delete(order);

        User user = userDao.getUserWithLock(order.getUser().getName());

        user.setReservedFunds(user.getReservedFunds().subtract(new BigDecimal(order.getPrice())));

        return user;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recomputeRating(String solvername) {
        User solver = userDao.findOne(solvername);

        long cnt = 0;
        long summ = 0;

        for (Order ord : orderDao.getOrdersWithReviews(solvername, 0L, 100L)) {
            if (!ord.getReview().getHide()) {
                cnt += 1;
                summ += ord.getReview().getRating();
            }
        }

        if (cnt != 0) {
            solver.setRating((1.0 * summ) / cnt);
        }

        userDao.update(solver);
    }
}

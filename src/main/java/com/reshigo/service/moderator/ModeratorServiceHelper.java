package com.reshigo.service.moderator;

import com.reshigo.dao.OrderDao;
import com.reshigo.dao.UserDao;
import com.reshigo.model.entity.Order;
import com.reshigo.model.entity.Payment;
import com.reshigo.model.entity.User;
import com.reshigo.model.entity.permanent.OrderStatusEnum;
import io.jsonwebtoken.lang.Collections;
import javafx.util.Pair;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

import static com.reshigo.model.entity.permanent.OrderStatusEnum.*;

/**
 * Created by dmitry103 on 06/01/2018.
 */

@Service
public class ModeratorServiceHelper {
    Logger logger = LoggerFactory.getLogger(ModeratorServiceHelper.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private OrderDao orderDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void actOnReview(Long orderId) {
        Order order = orderDao.findOne(orderId);

        if (order != null && order.getReview() != null) {
            order.getReview().setHide(!order.getReview().getHide());
        }

        orderDao.update(order);
    }
}

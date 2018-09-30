package com.reshigo.security;

import com.reshigo.dao.OrderDao;
import com.reshigo.dao.UserDao;
import com.reshigo.model.entity.Order;
import com.reshigo.model.entity.User;
import com.reshigo.model.entity.permanent.OrderStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dmitry103 on 14/08/16.
 */

@Component("OrderAccess")
public class OrderAccessComponent {
    @Autowired
    private OrderDao orderDao;

    @Autowired
    private UserDao userDao;

    @Transactional
    public boolean isAllowed(UserDetails details, Long orderId) {
        if (!details.isEnabled()) {
            return false;
        }

        Order order = orderDao.findOne(orderId);

        return order == null || order.getStatus().equals(OrderStatusEnum.AVAILABLE)
                || order.getUser().getName().equals(details.getUsername())
                || (order.getSolver() != null && order.getSolver().getName().equals(details.getUsername()));
    }

    @Transactional
    public boolean isUserAllowed(UserDetails details, Long orderId) {

        if (!details.isEnabled()) {
            return false;
        }

        Order order = orderDao.findOne(orderId);

        return (order == null || order.getUser().getName().equals(details.getUsername()));
    }

    @Transactional
    public boolean isSolverAllowed(UserDetails details, Long orderId) {
        if (!details.isEnabled()) {
            return false;
        }

        User user = userDao.findOne(details.getUsername());

        if (user == null) {
            return false;
        }

        Order order = orderDao.findOne(orderId);

        return (order == null ||
                (order.getSolver() != null && order.getSolver().getName().equals(details.getUsername())) ||
                (order.getStatus() == OrderStatusEnum.AVAILABLE && !user.getIsCustomer())
        );
    }
}

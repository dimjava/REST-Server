package com.reshigo.service;

import com.reshigo.dao.OrderDao;
import com.reshigo.dao.UserDao;
import com.reshigo.dao.permanent.ThemesDao;
import com.reshigo.model.entity.Chat;
import com.reshigo.model.entity.Order;
import com.reshigo.model.entity.User;
import com.reshigo.model.entity.permanent.OrderStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;

import static com.reshigo.model.entity.permanent.ChatStatusEnum.DISABLED;

@Service
public class UserOrdersDraftService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ThemesDao themesDao;

    @Transactional
    public Order createDefaultOrder(String username) throws Exception {
        Order order = new Order();

        User user = userDao.findOne(username);

        orderDao.get(username, true, 0L, Integer.MAX_VALUE * 1L, null, -1L, null, OrderStatusEnum.DRAFT)
                .forEach(order1 -> orderDao.delete(order1));

        order.setUser(user);
        order.setDate(new Timestamp(new Date().getTime()));
        order.setMaturityDate(new Timestamp(new Date().getTime() + (long) 1000 * 60 * 60 * 24));
        order.setStatus(OrderStatusEnum.DRAFT);
        order.setTheme(themesDao.findOne(0L));
        order.setComment(new byte[]{});
        order.setPrice(90L);
        Chat chat = new Chat();
        chat.getParticipants().add(user);
        chat.setStatus(DISABLED);
        order.setChat(chat);

        orderDao.save(order);

        return order;
    }
}

package com.reshigo.service;

import com.reshigo.dao.OrderDao;
import com.reshigo.dao.UserDao;
import com.reshigo.dao.permanent.SubjectsDao;
import com.reshigo.dao.permanent.ThemesDao;
import com.reshigo.model.entity.Chat;
import com.reshigo.model.entity.Order;
import com.reshigo.model.entity.Theme;
import com.reshigo.model.entity.User;
import com.reshigo.model.entity.permanent.ChatStatusEnum;
import com.reshigo.model.entity.permanent.OrderStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by dmitry103 on 11/09/2017.
 */
@Service
public class UserOrdersServiceTestHelper {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserDao userDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private ThemesDao themesDao;

    @Autowired
    private SubjectsDao subjectsDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertOrders() throws Exception {
        Theme th = applicationContext.getBean(Theme.class);
        subjectsDao.save(th.getSubject());
        themesDao.save(th);

        User dmitry = (User) applicationContext.getBean("userDmitry");
        User antony = (User) applicationContext.getBean("userAntony");

        antony.setFunds(new BigDecimal(1000));
        antony.setReservedFunds(new BigDecimal(240));

        userDao.save(antony);
        userDao.save(dmitry);

        Order order1 = new Order();
        order1.setId(1L);
        order1.setPrice(80L);
        order1.setUser(antony);
        order1.setStatus(OrderStatusEnum.AVAILABLE);
        order1.setDate(new Timestamp(new Date(2016, 1, 1).getTime()));
        order1.setMaturityDate(new Timestamp(new Date(2025, 1, 1).getTime()));
        order1.setTheme(th);
        Chat chat1 = new Chat();
        chat1.setStatus(ChatStatusEnum.DISABLED);
        order1.setChat(chat1);

        orderDao.save(order1);

        Order order2 = new Order();
    }
}

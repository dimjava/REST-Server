package com.reshigo.service;

import com.reshigo.dao.UserDao;
import com.reshigo.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dmitry103 on 10/09/2017.
 */

@Service
public class UserServiceTestHelper {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserDao userDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertUsers() throws Exception {
        User user = (User) applicationContext.getBean("userAntony");

        userDao.save(user);
    }
}

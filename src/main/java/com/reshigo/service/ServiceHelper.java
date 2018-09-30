package com.reshigo.service;

import com.reshigo.dao.UserDao;
import com.reshigo.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Created by dmitry103 on 29/08/2017.
 */

@Service
public class ServiceHelper {
    private final UserDao userDao;

    @Autowired
    public ServiceHelper(UserDao userDao) {
        this.userDao = userDao;
    }

    @Transactional
    public void transferFunds(String from, String to, BigDecimal price) {
        User user = userDao.getUserWithLock(from);
        User solver = userDao.getUserWithLock(to);

        BigDecimal bf = price.min(user.getBonusFunds());

        user.setFunds(user.getFunds().subtract(price));
        user.setReservedFunds(user.getReservedFunds().subtract(price));
        user.setBonusFunds(user.getBonusFunds().subtract(bf));

        solver.setFunds(solver.getFunds().add(price));
        solver.setBonusFunds(solver.getBonusFunds().add(bf));
    }
}

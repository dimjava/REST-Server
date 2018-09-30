package com.reshigo.security;

import com.reshigo.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by dmitry103 on 21/06/17.
 */

@Component("LoginComponent")
public class LoginComponent {
    @Autowired
    private UserDao userDao;

    @Transactional
    public boolean updateLastVisit(UserDetails details) {
        userDao.findOne(details.getUsername()).setLastVisit(new Timestamp(new Date().getTime()));

        return true;
    }
}

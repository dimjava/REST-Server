package com.reshigo.service;

import com.reshigo.dao.UserDao;
import com.reshigo.exception.NotAllowed;
import com.reshigo.exception.NotFound;
import com.reshigo.exception.ParamsError;
import com.reshigo.model.entity.User;
import com.reshigo.sms.SmsTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * Created by dmitry103 on 11/06/17.
 */

@Service
public class RootService {

    Logger logger = LoggerFactory.getLogger(RootService.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    @Qualifier("twilioMessageSender")
    private SmsTemplate twilioMessageSender;

    @Autowired
    @Qualifier("smsAero")
    private SmsTemplate smsAero;

    @Transactional
    public void passwordRecovery(String username, String phone) throws NotFound, Exception, NotAllowed {
        User user = getUser(username, phone);

        if (user == null) {
            throw new NotFound(null);
        }

        if (!user.isEnabled()) {
            throw new NotAllowed(null);
        }

        user.setCode(generateCode());
        user.setAttempts(0L);

        try {
            smsAero.send(user, "ReshiGo code: " + user.getCode() + "%0A" + "Account login: " + user.getName());
        } catch (Exception e) {
            logger.warn("SmsAero failed to send message to: {}", user.getPhone(), e);

            try {
                twilioMessageSender.send(user, "ReshiGo code: " + user.getCode() + "%0A" + "Account login: " + user.getName());
            } catch (Exception e1) {
                logger.error("Twilio failed to send message to: {}", user.getPhone(), e1);

                throw e1;
            }
        }
    }

    @Transactional
    public void resendPasswordRecoveryCode(String username, String phone) throws NotFound, Exception, NotAllowed {
        User user = getUser(username, phone);

        if (user == null) {
            throw new NotFound(null);
        }

        if (!user.isEnabled()) {
            throw new NotAllowed(null);
        }

        user.setCode(generateCode());

        try {
            twilioMessageSender.send(user, "ReshiGo code: " + user.getCode() + "%0A" + "Account login: " + user.getName());
        } catch (Exception e) {
            logger.error("Twilio failed to send message to: {}", user.getPhone(), e);

            throw e;
        }
    }

    @Transactional
    public boolean validateCode(Long code, String username, String phone) throws NotFound, NotAllowed {
        User user = getUser(username, phone);

        if (user == null) {
            throw new NotFound(null);
        }

        if (!user.isEnabled()) {
            throw new NotAllowed(null);
        }

        if (user.getAttempts() >= 4) {
            return false;
        }

        if (user.getCode() == null) {
            return false;
        }

        user.setAttempts(user.getAttempts() + 1);

        return user.getCode().equals(code);
    }

    @Transactional
    public boolean updatePassword(String username, String phone, String password, Long code) throws NotFound, NotAllowed, ParamsError {
        User user = getUser(username, phone);

        if (user == null) {
            throw new NotFound(null);
        }

        if (!user.isEnabled()) {
            throw new NotAllowed(null);
        }

        if (user.getAttempts() >= 3) {
            return false;
        }

        if (user.getCode() == null) {
            return false;
        }

        Pattern passwordPattern = Pattern.compile("[a-zA-Z0-9_/\\\\\\[\\]\\.\\{\\}\\-]+");
        if (!passwordPattern.matcher(password).matches()) {
            throw new ParamsError(null);
        }

        user.setAttempts(user.getAttempts() + 1);

        if (user.getCode().equals(code)) {
            user.setPassword(password);
            user.setCode(null);

            return true;
        }

        return false;
    }

    private User getUser(String username, String phone) {
        if (username != null) {
            return userDao.getUserWithLock(username);
        } else {
            if (!phone.startsWith("+")) {
                phone = "+" + phone;
            }

            List<User> users = userDao.findByPhone(phone);

            // find solver if several accounts
            if (users.size() == 1) {
                return users.get(0);
            } else if (users.size() == 2) {
                if (users.get(0).getIsCustomer()) {
                    return users.get(1);
                } else {
                    return users.get(0);
                }
            }
        }

        return null;
    }

    private Long generateCode() {
        Random generator = new Random();

        return 1000L + generator.nextInt(9000);
    }
}

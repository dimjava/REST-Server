package com.reshigo.service;

import com.reshigo.dao.PaymentDao;
import com.reshigo.dao.UserDao;
import com.reshigo.model.entity.Payment;
import com.reshigo.model.entity.User;
import com.reshigo.notifications.FundsChangeNotification;
import com.reshigo.notifications.NotificationMessagingTemplate;
import com.reshigo.payment.PaymentVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dmitry103 on 24/11/16.
 */

@Service
public class PaymentService {

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private UserDao userDao;

    @Autowired @Qualifier("feedService")
    private NotificationMessagingTemplate notificationMessagingTemplate;

    @Autowired
    private PaymentVerifier pv;

    @Transactional
    public String updateFunds(BigDecimal outSum, Long invId, String signatureValue) throws NoSuchAlgorithmException {
        pv.verify(outSum, invId, signatureValue);

        Payment payment = paymentDao.getPaymentWithLock(invId);

        if (payment.getCompleted()) {
            return "OK" + payment.getId().toString();
        }

        payment.setCompleted(true);

        User user = userDao.getUserWithLock(payment.getUser().getName());
        user.setFunds(user.getFunds().add(outSum));

        notificationMessagingTemplate.send(new FundsChangeNotification(payment.getId(), outSum.doubleValue(), user.getFunds().doubleValue()), user);

        return "OK" + payment.getId().toString();
    }
}

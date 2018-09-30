package com.reshigo.service;

import com.microsoft.azure.storage.StorageException;
import com.reshigo.dao.*;
import com.reshigo.exception.NotAllowed;
import com.reshigo.exception.NotAvailable;
import com.reshigo.exception.NotFound;
import com.reshigo.exception.registration.DuplicateEntityError;
import com.reshigo.exception.registration.DuplicatePhoneError;
import com.reshigo.model.entity.*;
import com.reshigo.notifications.NotificationMessagingTemplate;
import com.reshigo.payment.PaymentVerifier;
import com.reshigo.sms.SmsTemplate;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static javax.management.timer.Timer.ONE_HOUR;

@Service
public class UserService {
    @Autowired
    private UserDao userDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private FeedDao feedDao;

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private PaymentVerifier pv;

    @Autowired
    private UserServiceHelper helper;

    @Autowired
    @Qualifier("twilioMessageSender")
    private SmsTemplate twilioMessageSender;

    @Autowired
    @Qualifier("smsAero")
    private SmsTemplate smsAero;

    @Autowired
    @Qualifier("feedService")
    private NotificationMessagingTemplate notificationMessagingTemplate;

    @Transactional
    public void register(User user) throws DuplicateEntityError, DuplicatePhoneError, NotAvailable, NotAllowed, Exception {
        helper.register(user);

        try {
            smsAero.send(user, "ReshiGo registration code: " + user.getCode());
        } catch (Exception e) {

            try {
                twilioMessageSender.send(user, "ReshiGo registration code: " + user.getCode());
            } catch (Exception ex) {
                userDao.delete(user);
                ex.printStackTrace();

                throw new NotAvailable("Не удалось послать смс на ваш номер. Пожалуйста, обратитесь в поддержку и мы постараемся помочь.");
            }
        }
    }

    @Transactional
    public void registerSolver(User user) throws NotAllowed, DuplicateEntityError, DuplicatePhoneError, NotAvailable, Exception {
        helper.registerSolver(user);

        try {
            smsAero.send(user, "Your ReshiGo account is activated.%0ALogin: " + user.getName() + "%0APassword: " + user.getPassword());
        } catch (Exception e) {

            try {
                twilioMessageSender.send(user, "Your ReshiGo account is activated.%0ALogin: " + user.getName() + "%0APassword: " + user.getPassword());
            } catch (Exception ex) {
                userDao.delete(user);
                ex.printStackTrace();

                throw new NotAvailable("Не удалось послать смс на ваш номер. Пожалуйста, обратитесь в поддержку и мы постараемся помочь.");
            }
        }
    }

    @Transactional
    public void resendCode(String username) throws NotFound, NotAvailable, NotAllowed {
        User user = userDao.getUserWithLock(username);

        if (user == null) {
            throw new NotFound(null);
        }

        if (user.isEnabled()) {
            return;
        }

        user.setCode(helper.generateCode());

        try {
            twilioMessageSender.send(user, user.getCode().toString());
        } catch (Exception ex) {
            throw new NotAvailable(null);
        }
    }

    @Transactional
    public User getByNameWithAuthorities(String name) {
        User user = userDao.findOne(name);

        if (user != null) {
            Hibernate.initialize(user.getAuthorities());
        }

        return user;
    }

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Autowired
    private ApplicationContext context;

    @Transactional
    public User getByName(String name) {
        return userDao.findOne(name);
    }

    @Transactional
    public void logout(String name) {
        User user = userDao.getUserWithLock(name);

        for (Authorities a : user.getAuthorities()) {
            notificationMessagingTemplate.unsubscribe(user, a.getAuthority());
        }

        notificationMessagingTemplate.delete(user);

        user.setFcmToken(null);
    }

    @Transactional
    public void updateFcm(String token, String username) {
        User user = userDao.findOne(username);

        if (token.charAt(0) == '"') {
            token = token.substring(1, token.length() - 1);
        }

        if (!notificationMessagingTemplate.check(token)) {
            return;
        }

        user.setFcmToken(token);

        for (Authorities a : user.getAuthorities()) {
            notificationMessagingTemplate.subscribe(user, a.getAuthority());
        }
    }

    @Transactional
    public void subscribe(String topic, String username) {
        User user = userDao.findOne(username);

        notificationMessagingTemplate.subscribe(user, topic);
    }

    @Transactional
    public void unsubscribe(String topic, String username) {
        User user = userDao.findOne(username);

        notificationMessagingTemplate.unsubscribe(user, topic);
    }

    @Transactional
    public void setPromoCode(String promoCode, String userName) throws NotAllowed, NotAvailable, NotFound, Exception {
        User user = userDao.getUserWithLock(userName);

        if (user.getUsedPromo() != null) {
            throw new NotAllowed(null);
        }

        if (user.getPromocode() != null && user.getPromocode().equals(promoCode)) {
            throw new NotAllowed(null);
        }

        List<User> friends = userDao.findPromo(promoCode);

        if (friends.size() == 0) {
            throw new NotFound(null);
        }

        if (friends.size() > 1) {
            throw new NotAvailable(null);
        }

        if (!friends.get(0).isEnabled()) {
            throw new NotAllowed(null);
        }

        user.setUsedPromo(promoCode);

        //add money for both users

        BigDecimal r50 = new BigDecimal("50.00");

        BigDecimal tf = user.getFunds().add(r50);
        user.setFunds(tf);
        user.setBonusFunds(user.getBonusFunds().add(r50));

        User friend = userDao.getUserWithLock(friends.get(0).getName());

        tf = friend.getFunds().add(r50);
        friend.setFunds(tf);
        friend.setBonusFunds(friend.getBonusFunds().add(r50));

        //insert payments for bonus funds
        Payment payment1 = new Payment();
        payment1.setId(0L);
        payment1.setUser(user);
        payment1.setAmount(r50);
        payment1.setBonusAmount(r50);
        payment1.setCommission(BigDecimal.ZERO);
        payment1.setDate(new Timestamp(new Date().getTime()));
        paymentDao.save(payment1);

        Payment payment2 = new Payment();
        payment2.setId(0L);
        payment2.setUser(friend);
        payment2.setAmount(r50);
        payment2.setBonusAmount(r50);
        payment2.setCommission(BigDecimal.ZERO);
        payment2.setDate(new Timestamp(new Date().getTime()));
        paymentDao.save(payment2);
    }

    @Transactional
    public String increaseFunds(Long amount, String name) throws NotAllowed, Exception {
        BigDecimal bd = new BigDecimal(amount);

        User user = userDao.findOne(name);

        if (!user.getIsCustomer() || amount < 50 || amount > 5000) {
            throw new NotAllowed(null);
        }

        Payment payment = new Payment();
        payment.setId(0L);
        payment.setUser(user);
        payment.setAmount(bd);
        payment.setBonusAmount(new BigDecimal(BigInteger.ZERO, 2));
        payment.setDate(new Timestamp(new Date().getTime()));
        payment.setCommission(BigDecimal.ZERO);
        paymentDao.save(payment);

        return pv.createHtmlPage(payment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean verify(String code, String name) {
        User user = userDao.getUserWithLock(name);

        if (user == null) {
            return false;
        }

        if (user.getAttempts() > 2) {
            userDao.delete(user);

            return false;
        }

        if (code.equals(user.getCode().toString())) {
            user.setEnabled(true);
            user.setCode(null);

            userDao.update(user);

            return true;
        }

        user.setAttempts(user.getAttempts() + 1L);

        return false;
    }

    @Transactional
    public void updateInfo(User user, String username) {
        User u = userDao.findOne(username);

        if (user.getInfo() != null) {
            u.setInfo(user.getInfo());
        }

        if (user.getEducation() != null) {
            u.setEducation(user.getEducation());
        }

        if (user.getDegree() != null) {
            u.setDegree(user.getDegree());
        }

        if (user.isIosReview()) {
            u.setIosReview(true);
        }

        if (user.isAndroidReview()) {
            u.setAndroidReview(true);
        }
    }

    @Transactional
    public List<Feed> getFeeds(Long lastId, String username, MutableBoolean late) {

        User user = userDao.findOne(username);

        if (user == null) {
            return new LinkedList<>();
        }

        Timestamp curTime = new Timestamp(new Date().getTime());

        if (user.getFeedLoadTime() != null) {
            if (curTime.getTime() - user.getFeedLoadTime().getTime() > 6 * ONE_HOUR) {
                late.setValue(true);
            }
        }

        user.setFeedLoadTime(curTime);

        return feedDao.getFeeds(lastId, username);
    }

    @Transactional
    public void updatePhoto(String name, byte[] file) throws IOException, URISyntaxException, StorageException {
        User user = userDao.findOne(name);

        if (user.getPhoto() != null) {
            fileUtil.deleteImage(user.getPhoto());
        }

        String path = fileUtil.saveProfileImage(name, file);
        user.setPhoto(path);
    }

    @Transactional
    public byte[] getProfilePhoto(String name) throws NotFound, IOException, URISyntaxException, StorageException {
        User user = userDao.findOne(name);

        if (user == null || user.getPhoto() == null) {
            throw new NotFound(null);
        }

        return fileUtil.getImage(user.getPhoto());
    }

    @Transactional
    public List<Review> getReviews(String solvername, Long offset, Long limit) {
        User solver = userDao.findOne(solvername);

        if (solver.getIsCustomer()) {
            return Collections.emptyList();
        }

        List<Order> orders = orderDao.getOrdersWithReviews(solvername, offset, limit);
        List<Review> reviews = orders.stream().map(Order::getReview).filter(r -> r != null && !r.getHide()).collect(Collectors.toList());
        reviews.forEach(Hibernate::initialize);

        return reviews;
    }

    @Transactional
    public List<Payment> getPayments(String name) {
        return (List<Payment>) paymentDao.getSession().createCriteria(Payment.class)
                .add(Restrictions.eq("completed", true))
                .add(Restrictions.eq("user.name", name))
                .list();
    }
}

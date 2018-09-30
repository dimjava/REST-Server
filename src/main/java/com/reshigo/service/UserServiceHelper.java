package com.reshigo.service;

import com.reshigo.dao.PaymentDao;
import com.reshigo.dao.UserDao;
import com.reshigo.exception.NotAllowed;
import com.reshigo.exception.NotAvailable;
import com.reshigo.exception.registration.DuplicateEntityError;
import com.reshigo.exception.registration.DuplicatePhoneError;
import com.reshigo.model.entity.Authorities;
import com.reshigo.model.entity.Payment;
import com.reshigo.model.entity.User;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by dmitry103 on 24/11/2017.
 */

@Service
public class UserServiceHelper {
    @Autowired
    private UserDao userDao;

    @Autowired
    private PaymentDao paymentDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void register(User user) throws NotAllowed, DuplicateEntityError, DuplicatePhoneError, Exception {
        checkNameAndPassword(user);

        User foundUser = userDao.findOne(user.getName());

        if (foundUser != null && foundUser.isEnabled()) {
            throw new DuplicateEntityError("Пользователь с данным именем уже существует");
        }

        if (foundUser != null && !foundUser.isEnabled()) {
            userDao.delete(foundUser);
        }

        List<User> fndUsers = userDao.findByPhone(user.getPhone());

        if (fndUsers.size() > 1) {
            throw new DuplicatePhoneError("Пользователь с данным номером телефона уже существует");
        }

        if (!fndUsers.isEmpty()) {
            if (fndUsers.get(0).getIsCustomer()) {
                throw new DuplicatePhoneError("Пользователь с данным номером телефона уже существует");
            }
        }

        Authorities authorities = new Authorities();
        authorities.setUser(user);
        authorities.setAuthority("USER");

        user.getAuthorities().add(authorities);
        user.setIsCustomer(true);
        user.setEnabled(false);
        user.setRegistrationDate(new Timestamp(new Date().getTime()));
        user.setRating(5.0);
        user.setCode(generateCode());
        user.setAttempts(0L);
        user.setCommission(BigDecimal.ZERO);
//        user.setFunds(new BigDecimal("100"));
        user.setFunds(new BigDecimal(BigInteger.ZERO, 2));
//        user.setReservedFunds(new BigDecimal(BigInteger.ZERO, 2));
        user.setReservedFunds(new BigDecimal(BigInteger.ZERO, 2));
//        user.setBonusFunds(new BigDecimal("100"));
        user.setBonusFunds(new BigDecimal(BigInteger.ZERO, 2));
        user.setPromocode(RandomStringUtils.randomAlphanumeric(10));
        userDao.save(user);

        //insert payment for bonus funds
//        Payment payment = new Payment();
//        payment.setId(0L);
//        payment.setUser(user);
//        payment.setAmount(user.getFunds());
//        payment.setBonusAmount(user.getBonusFunds());
//        payment.setCommission(BigDecimal.ZERO);
//        payment.setCompleted(true);
//        payment.setDate(new Timestamp(new Date().getTime()));
//        paymentDao.save(payment);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerSolver(User user) throws NotAllowed, DuplicateEntityError, DuplicatePhoneError, NotAvailable, Exception {
        user.setIsCustomer(false);
        checkNameAndPassword(user);

        User foundUser = userDao.findOne(user.getName());

        if (foundUser != null && foundUser.isEnabled()) {
            throw new DuplicateEntityError("Пользователь с данным именем уже существует");
        }

        List<User> fndUsers = userDao.findByPhone(user.getPhone());

        if (fndUsers.size() > 1) {
            throw new DuplicatePhoneError("Исполнитель с данным номером телефона уже существует");
        }

        if (!fndUsers.isEmpty()) {
            if (!fndUsers.get(0).getIsCustomer()) {
                throw new DuplicatePhoneError("Исполнитель с данным номером телефона уже существует");
            }
        }

        Authorities authorities = new Authorities();
        authorities.setUser(user);
        authorities.setAuthority("SOLVER");

        user.getAuthorities().add(authorities);
        user.setEnabled(true);
        user.setRegistrationDate(new Timestamp(new Date().getTime()));

        user.setCode(generateCode());
        user.setAttempts(0L);

        user.setFunds(new BigDecimal(BigInteger.ZERO, 2));
        user.setReservedFunds(new BigDecimal(BigInteger.ZERO, 2));
        user.setBonusFunds(new BigDecimal(BigInteger.ZERO, 2));
        user.setCommission(new BigDecimal("0.3"));
        user.setPromocode(RandomStringUtils.randomAlphanumeric(10));

        userDao.save(user);
    }

    private void checkNameAndPassword(User user) throws NotAllowed {

        ArrayList<String> restrictedNames = new ArrayList<>(Arrays.asList("user", "solver", "admin", "moderator"));

        if (restrictedNames.contains(user.getName().toLowerCase())) {
            throw new NotAllowed("Пользователь с данным именем уже существует");
        }

        Pattern phonePattern = Pattern.compile("\\+7\\d{10}");
        if (user.getIsCustomer() && !phonePattern.matcher(user.getPhone()).matches()) {
            throw new NotAllowed("Укажете корректный номер телефона");
        }

        Pattern namePattern = Pattern.compile("[a-zA-Z0-9_/\\\\\\[\\]\\.\\{\\}\\-]+");
        if (!namePattern.matcher(user.getName()).matches()) {
            throw new NotAllowed("Имя пользователя не может содержать никакие символы кроме: " +
                    "латинских букв, цифр и символов '\', '{', '}', '.', '_', '-'");
        }

        Pattern onlyDigits = Pattern.compile("\\d+");
        if (onlyDigits.matcher(user.getName()).matches()) {
            throw new NotAllowed("Имя пользователя не может содержать только цифры");
        }

        if (!namePattern.matcher(user.getPassword()).matches()) {
            throw new NotAllowed("Пароль не может содержать никакие символы кроме: " +
                    "латинских букв, цифр и символов '\\', '{', '}', '.', '_', '-'");
        }

    }

    public Long generateCode() {
        Random generator = new Random();

        return 1000L + generator.nextInt(9000);
    }
}

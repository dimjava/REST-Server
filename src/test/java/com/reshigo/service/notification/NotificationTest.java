package com.reshigo.service.notification;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reshigo.ServiceTestContext;
import com.reshigo.dao.PaymentDao;
import com.reshigo.dao.UserDao;
import com.reshigo.model.entity.Order;
import com.reshigo.model.entity.Payment;
import com.reshigo.model.entity.User;
import com.reshigo.model.entity.permanent.OrderStatusEnum;
import com.reshigo.notifications.FCMNotificationMessagingTemplate;
import com.reshigo.notifications.TextNotification;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by dmitry103 on 30/08/16.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {ServiceTestContext.class})
@WebAppConfiguration
public class NotificationTest {
    @Test
    public void fcmNotifTest() throws JsonProcessingException {
        Properties properties = new Properties();
        properties.setProperty("fcm.auth", "");
        properties.setProperty("fcm.url", "https://fcm.googleapis.com/fcm/send");
        properties.setProperty("fcm.iidUrl", "https://iid.googleapis.com/iid/v1/");

        FCMNotificationMessagingTemplate ns = new FCMNotificationMessagingTemplate(properties);

        TextNotification txt = new TextNotification("", "Ваш заказ зарезервирован");

        System.out.println(new ObjectMapper().writeValueAsString(txt));

        User u = new User();
        u.setFcmToken("");
        //u.setFcmToken("");
        //u.setFcmToken("");

        //ns.subscribe(u, "test");
        //ns.send(txt, "test");

        //System.out.print(ns.check(u.getFcmToken()));

        ns.send(txt, u);
    }

    @Test
    public void objectMapperTest() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.USE_ANNOTATIONS, false);

        ObjectMapper mapper1 = new ObjectMapper();

        User user = new User();
        user.setFcmToken("skajd;lfjsaldjflsdkfjlsd");

        System.out.println(mapper.writeValueAsString(user));
        System.out.println(mapper1.writeValueAsString(user));
    }

    @Autowired
    private UserDao userDao;

    @Autowired
    private PaymentDao paymentDao;

    @Test
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void testTest() {

        List<User> users = userDao.getUsers(1L, 1L, "", "");

        for (User u :
                users) {
            System.out.println(u.getName());

            BigDecimal payed = BigDecimal.ZERO;

            for (Payment p :
                    u.getPayments()) {
                payed = payed.add(p.getAmount());
            }

            if (payed.compareTo(BigDecimal.ZERO) < 0) {
                continue;
            }

            BigDecimal spent = BigDecimal.ZERO;

            for (Order order : u.getOrders()) {
                if (order.getStatus().equals(OrderStatusEnum.DONE)) {
                    spent = spent.add(new BigDecimal(order.getPrice()));
                }
            }

            BigDecimal balance = u.getFunds().add(spent).subtract(payed);

            if (balance.compareTo(BigDecimal.ZERO) < 0) {
                continue;
            }

            System.out.println(balance.intValue());

            Payment payment = new Payment();
            payment.setId(0L);
            payment.setUser(u);
            payment.setAmount(balance);
            payment.setBonusAmount(balance);
            payment.setCommission(BigDecimal.ZERO);
            payment.setCompleted(true);
            payment.setDate(new Timestamp(new Date().getTime()));

            try {
                u.getPayments().add(payment);
                userDao.update(u);
                paymentDao.save(payment);
            } catch (Exception e) {
                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> EXCEPTION <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
            }

//            break;
        }
    }
}

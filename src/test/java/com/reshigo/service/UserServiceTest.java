package com.reshigo.service;

import com.reshigo.ServiceTestContext;
import com.reshigo.dao.PaymentDao;
import com.reshigo.dao.UserDao;
import com.reshigo.exception.NotAllowed;
import com.reshigo.exception.NotAvailable;
import com.reshigo.exception.registration.DuplicateEntityError;
import com.reshigo.exception.registration.DuplicatePhoneError;
import com.reshigo.model.entity.Payment;
import com.reshigo.model.entity.User;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = {ServiceTestContext.class})
@WebAppConfiguration
public class UserServiceTest extends DBUnitConfig {
    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private UserService userService;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private UserServiceTestHelper helper;

    @Before
    public void setUp() throws Exception {
//        dataSet = new FlatXmlDataSet(Thread.currentThread().getContextClassLoader().getResourceAsStream("com/reshigo/service/user-dataset.xml"));
//        super.setUp();
    }


    @Test
    @Transactional
    public void testRegister() throws Exception, DuplicateEntityError, DuplicatePhoneError, NotAvailable, NotAllowed {
        User user = new User();
        user.setPhone("+79260000002");
        user.setPassword("pass");
        user.setEnabled(true);
        user.setEmail("");
        user.setName("dmitry");
        user.setIsCustomer(true);

        userService.register(user);

        User inserted = userDao.findOne("dmitry");
        assertNotNull(inserted);
        assertEquals(user.getPhone(), inserted.getPhone());
        assertEquals(user.getPassword(), inserted.getPassword());
        assertEquals(false, inserted.isEnabled());
        assertEquals(true, inserted.getIsCustomer());
        assertEquals(new BigDecimal("100.00"), inserted.getFunds());
        assertEquals(new BigDecimal("100.00"), inserted.getBonusFunds());

        assertNotNull(inserted.getPayments());
        assertEquals(1, inserted.getPayments().size());
        assertNotNull(inserted.getPayments().get(0));
        assertEquals(new BigDecimal("100.00"), inserted.getPayments().get(0).getAmount());
        assertEquals(new BigDecimal("100.00"), inserted.getPayments().get(0).getBonusAmount());
        assertEquals(true, inserted.getPayments().get(0).getCompleted());
        assertEquals(0.0, inserted.getPayments().get(0).getCommission());
    }

    @Test(expected = DuplicateEntityError.class)
    @Transactional
    public void testDuplicateRegister() throws DuplicateEntityError, DuplicatePhoneError, NotAvailable, NotAllowed, Exception {
        helper.insertUsers();

        User user = (User) applicationContext.getBean("userAntony");

        User dp = new User();
        dp.setName(user.getName());
        dp.setPassword(user.getPassword());
        dp.setPhone(user.getPhone());
        dp.setEmail(user.getEmail());

        userService.register(dp);
    }

    @Test(expected = DuplicatePhoneError.class)
    @Transactional
    public void testDuplicatePhoneRegister() throws DuplicateEntityError, DuplicatePhoneError, NotAvailable, NotAllowed, Exception {
        helper.insertUsers();

        User user = new User();
        user.setName("Vivaldi");
        user.setEmail("somemail@email.ru");
        user.setEnabled(true);
        user.setPassword("password");
        user.setPhone(((User) applicationContext.getBean("userAntony")).getPhone());
        user.setIsCustomer(true);

        userService.register(user);
    }
}

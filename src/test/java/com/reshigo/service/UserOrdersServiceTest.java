package com.reshigo.service;

import com.reshigo.ServiceTestContext;
import com.reshigo.exception.NotAllowed;
import com.reshigo.exception.NotAvailable;
import com.reshigo.exception.NotFound;
import com.reshigo.exception.ParamsError;
import com.reshigo.model.entity.Order;
import com.reshigo.model.entity.Picture;
import com.reshigo.model.entity.PictureData;
import com.reshigo.model.entity.Theme;
import org.dbunit.Assertion;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ContextConfiguration(classes = {ServiceTestContext.class})
@WebAppConfiguration
public class UserOrdersServiceTest extends DBUnitConfig {
    //@Autowired
    private UserOrdersService userOrdersService;

    @Autowired
    private UserOrdersServiceTestHelper helper;

    @Before
    public void setUp() throws Exception {
        dataSet = new FlatXmlDataSet(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("com/reshigo/service/user-dataset.xml"));

        super.setUp();
    }

    @After
    public void clean() throws Exception {
        super.tearDown();
    }


    @Test
    public void testSendOrderToAvailable() throws NotFound, NotAvailable, Exception, NotAllowed, ParamsError {
        Order order = new Order();
        order.setMaturityDate(Timestamp.valueOf("2025-01-01 18:00:00.00"));
        order.setId(4L);

        Theme theme = new Theme();
        theme.setId(0L);
        order.setTheme(theme);
        order.setPrice(80L);

        userOrdersService.sendOrderToAvailable(order);

        IDataSet actual = getConnection().createDataSet();
        IDataSet expected = new FlatXmlDataSet(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("com/reshigo/service/userorders-dataset-sendavailable.xml"));

        String[] ignoreOrders = {"date", "comment", "solvername"};

        Assertion.assertEqualsIgnoreCols(expected.getTable("orders"), actual.getTable("orders"), ignoreOrders);
        Assertion.assertEquals(expected.getTable("pictures"), actual.getTable("pictures"));
    }

    @Test(expected = NotAvailable.class)
    public void testNotAvailableSendOrderToAvailable() throws NotFound, NotAvailable, NotAllowed, ParamsError, Exception {
        Order order = new Order();
        order.setId(2L);

        helper.insertOrders();

        //userOrdersService.sendOrderToAvailable(order);
    }

    @Test(expected = NotAllowed.class)
    public void testExceedFundsSendingAvailable() throws NotAvailable, NotAllowed, NotFound, ParamsError {
        Order order = new Order();
        order.setId(4L);
        order.setPrice(1800L);

        userOrdersService.sendOrderToAvailable(order);
    }

    @Test(expected = NotFound.class)
    public void testNotFoundSendOrderToAvailable() throws NotFound, NotAvailable, NotAllowed, ParamsError {
        Order order = new Order();
        order.setId(100L);

        userOrdersService.sendOrderToAvailable(order);
    }

    @Test
    public void testDeleteOrder() throws NotAllowed, Exception, NotFound {
        userOrdersService.deleteOrder(1L);

        IDataSet actual = getConnection().createDataSet();
        IDataSet expected = new FlatXmlDataSet(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("com/reshigo/service/userorders-dataset-delete-ok.xml"));

        String[] ignoreOrders = {"date", "comment", "solvername"};

        Assertion.assertEqualsIgnoreCols(expected.getTable("orders"), actual.getTable("orders"), ignoreOrders);
        Assertion.assertEquals(expected.getTable("pictures"), actual.getTable("pictures"));
        Assertion.assertEquals(expected.getTable("chats"), actual.getTable("chats"));
        Assertion.assertEquals(expected.getTable("chats_users"), actual.getTable("chats_users"));
    }

    @Test(expected = NotAllowed.class)
    public void testReservedDeleteOrder() throws NotAllowed, NotFound {
        userOrdersService.deleteOrder(3L);
    }

    @Test
    public void testAddPicture() throws NotFound, NotAvailable, Exception {
        Picture picture = new Picture();
        PictureData pictureData = new PictureData();

        pictureData.setPicture(picture);
        pictureData.setData("some bytes".getBytes());

        picture.setCounter(2L);
        picture.setId(0);
        picture.setImg(pictureData);

        userOrdersService.addOrderPicture(picture, 4L);

        IDataSet actual = getConnection().createDataSet();
        IDataSet expected = new FlatXmlDataSet(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("com/reshigo/service/userorders-add-picture.xml"));

        Assertion.assertEqualsIgnoreCols(expected.getTable("pictures"), actual.getTable("pictures"), new String[]{"id", "img"});
        assertEquals(expected.getTable("pictures_data").getRowCount(), actual.getTable("pictures").getRowCount());
        assertEquals(picture.getId().toString(), actual.getTable("pictures").getValue(4, "id").toString());
    }

    @Test
    public void testDeleteOrderPicture() throws NotFound, Exception, NotAllowed {
        userOrdersService.deleteOrderPicture(4L, 4L);

        IDataSet actual = getConnection().createDataSet();
        IDataSet expected = new FlatXmlDataSet(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("com/reshigo/service/userorders-deletepicture.xml"));

        Assertion.assertEquals(expected.getTable("pictures"), actual.getTable("pictures"));
        Assertion.assertEquals(expected.getTable("pictures_data"), actual.getTable("pictures_data"));
    }

    @Test(expected = NotFound.class)
    public void testDeletePictureNotFound() throws NotFound, NotAllowed {
        userOrdersService.deleteOrderPicture(4L, 5L);
    }

    @Test(expected = NotAllowed.class)
    public void testDeletePictureNotAvailable() throws NotFound, NotAllowed {
        userOrdersService.deleteOrderPicture(2L, 10L);
    }

    @Test(expected = NotFound.class)
    public void updateMaturityDateNotFoundTest() throws NotFound, NotAllowed {
        userOrdersService.updateMaturityDate(10L, 222L);
    }

    @Test(expected = NotAllowed.class)
    public void updateMaturityDateNotAllowedTest() throws NotFound, NotAllowed {
        userOrdersService.updateMaturityDate(1L, 2L);
    }

    @Test
    public void updateMaturityDateTest() throws Exception, NotFound, NotAllowed {
        // add 5 minutes
        Timestamp tm = new Timestamp(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ss").parse("2025-01-01 18:00:00.00").getTime()
                + 1000 * 60);

        userOrdersService.updateMaturityDate(3L, 60000L);

        IDataSet actual = getConnection().createDataSet();

        assertEquals(tm, actual.getTable("orders").getValue(2, "maturity_date"));
    }

    @Test
    public void closeOrderDone() throws NotAvailable, NotAllowed, NotFound, Exception {
        IDataSet beforeTest = getConnection().createDataSet();
        BigDecimal reservedFundsBeforeTest = (BigDecimal) beforeTest.getTable("users").getValue(0, "reserved_funds");
        BigDecimal fundsBeforeTest = (BigDecimal) beforeTest.getTable("users").getValue(0, "funds");
        BigDecimal price = new BigDecimal((BigInteger) beforeTest.getTable("orders").getValue(1, "price"));
        BigDecimal solverFundsBeforeTest = (BigDecimal) beforeTest.getTable("users").getValue(1, "funds");

        userOrdersService.closeOrderDone(3L);

        IDataSet actual = getConnection().createDataSet();

        assertEquals("DONE", (String) actual.getTable("orders").getValue(2, "status"));
        assertEquals(fundsBeforeTest.subtract(price), actual.getTable("users").getValue(0, "funds"));
        assertEquals(reservedFundsBeforeTest.subtract(price), actual.getTable("users").getValue(0, "reserved_funds"));
        assertEquals(solverFundsBeforeTest.add(price), actual.getTable("users").getValue(1, "funds"));
    }

    @Test(expected = NotAvailable.class)
    public void acceptNotAvailableTest() throws NotAvailable, NotAllowed, NotFound {
        userOrdersService.closeOrderDone(2L);
    }

    @Test(expected = NotAvailable.class)
    public void rejectNotAvailableTest() throws NotAvailable, NotAllowed, NotFound {
        userOrdersService.rejectOrder(2L);
    }

    @Test
    public void appealTest() throws NotFound, NotAvailable, Exception {
        userOrdersService.appeal(2L);

        IDataSet actual = getConnection().createDataSet();

        assertEquals("REJECTED_APPEAL", actual.getTable("orders").getValue(1, "status"));
    }

    @Test(expected = NotAvailable.class)
    public void appealNotAvailable() throws NotFound, NotAvailable {
        userOrdersService.appeal(5L);
    }

    @Test
    public void confirmRejectedTest() throws Exception, NotFound, NotAvailable {
        IDataSet beforeTest = getConnection().createDataSet();
        BigDecimal reservedFundsBeforeTest = (BigDecimal) beforeTest.getTable("users").getValue(0, "reserved_funds");
        BigDecimal price = new BigDecimal((BigInteger) beforeTest.getTable("orders").getValue(1, "price"));
        BigDecimal fundsBeforeTest = (BigDecimal) beforeTest.getTable("users").getValue(0, "funds");

        userOrdersService.confirmRejected(2L);

        IDataSet actual = getConnection().createDataSet();

        assertEquals("REJECTED_CONF", (String) actual.getTable("orders").getValue(1, "status"));
        assertEquals("CLOSED", (String) actual.getTable("chats").getValue(1, "status"));
        assertEquals(reservedFundsBeforeTest.subtract(price), actual.getTable("users").getValue(0, "reserved_funds"));
        assertEquals(fundsBeforeTest, actual.getTable("users").getValue(0, "funds"));
    }

    @Test
    public void rejectTest() throws Exception, NotAvailable, NotAllowed, NotFound {
        IDataSet beforeTest = getConnection().createDataSet();
        BigDecimal reservedFundsBeforeTest = (BigDecimal) beforeTest.getTable("users").getValue(0, "reserved_funds");

        userOrdersService.rejectOrder(5L);

        IDataSet actual = getConnection().createDataSet();

        assertEquals("REJECTED", (String) actual.getTable("orders").getValue(4, "status"));
        assertEquals("CLOSED", (String) actual.getTable("chats").getValue(4, "status"));
        assertEquals(reservedFundsBeforeTest, actual.getTable("users").getValue(0, "reserved_funds"));
    }
}

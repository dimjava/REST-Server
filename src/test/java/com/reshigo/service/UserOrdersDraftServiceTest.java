package com.reshigo.service;

import com.reshigo.ServiceTestContext;
import com.reshigo.model.entity.Order;
import org.dbunit.Assertion;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = {ServiceTestContext.class})
@WebAppConfiguration
public class UserOrdersDraftServiceTest extends DBUnitConfig {
    @Autowired
    private UserOrdersDraftService userOrdersDraftService;

    @Before
    public void setUp() throws Exception {
        dataSet = new FlatXmlDataSet(Thread.currentThread().getContextClassLoader().getResourceAsStream("com/reshigo/service/userordersdraft-dataset.xml"));

        super.setUp();
    }

    @Test
    public void createDefaultOrderTest() throws Exception {
        Order order = userOrdersDraftService.createDefaultOrder("antony");

        assertNotNull(order);
        assertNotNull(order.getId());

        IDataSet actual = getConnection().createDataSet();
        IDataSet expected = new FlatXmlDataSet(Thread.currentThread().getContextClassLoader().getResourceAsStream("com/reshigo/service/userordersdraft-dataset-createdraft.xml"));

        String[] ignoreOrders = {"id", "date", "maturity_date", "comment", "solvername", "theme_id", "chat_id"};

        Assertion.assertEqualsIgnoreCols(expected.getTable("orders"), actual.getTable("orders"), ignoreOrders);
        assertEquals(actual.getTable("chats").getRowCount(), 1);
        assertEquals(actual.getTable("chats").getValue(0, "id").toString(), order.getChat().getId().toString());
        assertEquals(actual.getTable("chats_users").getValue(0, "chats_id").toString(), order.getChat().getId().toString());
        assertEquals(actual.getTable("chats_users").getValue(0, "participants_name"), "antony");
    }
}

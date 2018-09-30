package com.reshigo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reshigo.ControllerTestContext;
import com.reshigo.model.entity.Order;
import com.reshigo.model.entity.Theme;
import com.reshigo.model.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(classes = {ControllerTestContext.class})
@WebAppConfiguration
public class UserOrdersControllerTest {
    @Autowired
    private UserOrdersController userOrdersController;

    @Test
    public void testPostOrderBigCommentAndNormalComment() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(userOrdersController).build();

        User user = new User();
        user.setName("antony");

        Order order = new Order();
        order.setUser(user);

        order.setDate(new Timestamp(new Date().getTime()));
        order.setMaturityDate(new Timestamp(new Date().getTime()));

        Theme th = new Theme();
        th.setId(0L);
        order.setTheme(th);

        order.setPrice(100L);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100000; i++) {
            sb.append("a");
        }

        order.setComment(sb.toString().getBytes());

        ObjectMapper om = new ObjectMapper();
        mockMvc.perform(post("/user/orders").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(order)))
                .andExpect(status().is(400));

        order.setComment("hello".getBytes());
        mockMvc.perform(post("/user/orders").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsString(order)))
                .andExpect(status().is(201));
    }
}

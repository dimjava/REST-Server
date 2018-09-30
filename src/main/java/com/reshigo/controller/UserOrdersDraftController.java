package com.reshigo.controller;

import com.reshigo.model.entity.Order;
import com.reshigo.service.UserOrdersDraftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user/orders/draft")
public class UserOrdersDraftController {
    @Autowired
    private UserOrdersDraftService draftService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Order> createDefaultDraft(HttpServletRequest request) {
        Order order = null;
        try {
            order = draftService.createDefaultOrder(request.getUserPrincipal().getName());
        } catch (Exception e) {
            return new ResponseEntity<>(order, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }
}

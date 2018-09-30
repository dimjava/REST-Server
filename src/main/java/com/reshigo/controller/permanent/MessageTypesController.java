package com.reshigo.controller.permanent;

import com.reshigo.model.entity.MessageType;
import com.reshigo.service.permanent.MessageTypesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by dmitry103 on 24/07/16.
 */

@RestController
@RequestMapping(value = "/messageTypes")
public class MessageTypesController {
    @Autowired
    private MessageTypesService messageTypesService;

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<MessageType>> getMessageTypes() {
        return new ResponseEntity<>(messageTypesService.messageTypeList(), HttpStatus.OK);
    }
}

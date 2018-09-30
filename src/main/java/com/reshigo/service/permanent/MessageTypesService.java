package com.reshigo.service.permanent;

import com.reshigo.dao.permanent.MessageTypeDao;
import com.reshigo.model.entity.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by dmitry103 on 24/07/16.
 */

@Service
public class MessageTypesService {
    @Autowired
    private MessageTypeDao messageTypeDao;

    @Transactional
    public List<MessageType> messageTypeList() {
        return messageTypeDao.getAll();
    }
}

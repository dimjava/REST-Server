package com.reshigo.dao.permanent;

import com.reshigo.dao.AbstractEntityDao;
import com.reshigo.model.entity.MessageType;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by dmitry103 on 23/07/16.
 */

@Service
public class MessageTypeDao extends AbstractEntityDao<MessageType, Long> {
    @Autowired
    public MessageTypeDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public MessageType findOne(Long id) {
        return (MessageType) sessionFactory.getCurrentSession().get(MessageType.class, id);
    }

    public List<MessageType> getAll() {
        return sessionFactory.getCurrentSession().createCriteria(MessageType.class).list();
    }
}

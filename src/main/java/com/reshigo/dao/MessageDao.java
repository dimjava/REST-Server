package com.reshigo.dao;

import com.microsoft.azure.storage.StorageException;
import com.reshigo.model.entity.Chat;
import com.reshigo.model.entity.Message;
import com.reshigo.model.entity.Picture;
import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.persistence.PrePersist;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;

/**
 * Created by dmitry103 on 17/07/16.
 */

@Service
public class MessageDao {
    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    private FileUtil fileUtil;

    @PrePersist
    public void prePersist(Message message) {
        if (message.getWidth() == null || message.getWidth() <= 0) {
            message.setWidth(Message.DEFAULT_WIDTH);
        }

        if (message.getHeight() == null || message.getHeight() <= 0) {
            message.setHeight(Message.DEFAULT_HEIGHT);
        }
    }

    public Message findOne(Long messageId) throws IllegalAccessException {
        List<Message> messages = sessionFactory.getCurrentSession().createCriteria(Message.class).add(Restrictions.eq("id", messageId)).list();

        if (messages.isEmpty()) {
            return null;
        }

        if (messages.get(0).getPath() == null) {
            return messages.get(0);
        }

        Message cl = messages.get(0).cloneNoData();

        try {
            cl.setData(fileUtil.getImage(cl.getPath()));
        } catch (IOException|StorageException|URISyntaxException e) {
            return null;
        }


        return cl;
    }

    public List<Message> get(Long chatId, Long date) {
        Criteria criteria = sessionFactory.getCurrentSession().createCriteria(Message.class);

        criteria.add(Restrictions.eq("chat.id", chatId));
        criteria.add(Restrictions.ge("date", new Date(date)));

        return criteria.list();
    }

    public void save(Message message) throws IOException, URISyntaxException, StorageException {
        byte[] data = message.getData();

        if (!message.getMessageType().getId().equals(0L)) {
            message.setData(new byte[] {});
        }

        sessionFactory.getCurrentSession().save(message);

        if (!message.getMessageType().getId().equals(0L)) {
            message.setPath(fileUtil.saveMessageImage(message.getChat().getId(), message.getId(), data));
        }
    }
}

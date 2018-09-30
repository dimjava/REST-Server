package com.reshigo.dao;

import com.reshigo.model.entity.Chat;
import com.reshigo.model.entity.Message;
import com.reshigo.model.entity.Order;
import com.reshigo.model.entity.permanent.ChatStatusEnum;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by dmitry103 on 17/07/16.
 */

@Service
public class ChatDao extends AbstractEntityDao<Chat, Long> {
    @Autowired
    public ChatDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Chat findOne(Long chatId) {
        List<Chat> chats = sessionFactory.getCurrentSession().createCriteria(Chat.class).add(Restrictions.eq("id", chatId)).list();

        if (chats.isEmpty()) {
            return null;
        }

        return chats.get(0);
    }

    public Message getLastRead(Long chatId, String username) {
        List<Message> messages = sessionFactory.getCurrentSession().createCriteria(Message.class)
                .add(Restrictions.eq("chat.id", chatId))
                .add(Restrictions.eq("user.name", username))
                .add(Restrictions.eq("isRead", true))
                .addOrder(org.hibernate.criterion.Order.desc("id"))
        .list();

        if (!messages.isEmpty()) {
            return messages.get(0);
        }

        return new Message();
    }

    public void setRead(Long chatId, Long lastId, String name) {
        sessionFactory.getCurrentSession().createQuery("UPDATE Message SET isRead = 1 where user.name != :username and chat_id = :chatId and id <= :lastMessageId")
                .setParameter("username", name)
                .setParameter("lastMessageId", lastId)
                .setParameter("chatId", chatId)
        .executeUpdate();
    }

    public List<Chat> getChats(String name, String[] statuses) {
        List<Chat> chats = sessionFactory.getCurrentSession()
                .createQuery("select ch From Chat ch join ch.participants p where status in (:statuses) and p.name = :name")
                .setParameterList("statuses", statuses)
                .setParameter("name", name).list();

        return chats;
    }
}

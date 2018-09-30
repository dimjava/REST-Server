package com.reshigo.security;

import com.reshigo.dao.ChatDao;
import com.reshigo.dao.MessageDao;
import com.reshigo.model.entity.Chat;
import com.reshigo.model.entity.Message;
import com.reshigo.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by dmitry103 on 14/08/16.
 */
@Component("ChatAccess")
public class ChatAccessComponent {
    @Autowired
    private ChatDao chatDao;

    @Autowired
    MessageDao messageDao;

    @Transactional
    public boolean isAllowed(UserDetails details, Long id) {
        Chat chat = chatDao.findOne(id);

        if (chat == null) {
            return true;
        }

        for (User participant : chat.getParticipants()) {
            if (participant.getName().equals(details.getUsername())) {
                return true;
            }
        }

        return false;
    }

    @Transactional
    public boolean isMessageAllowed(UserDetails details, Long messageId) {
        Message message = null;
        try {
            message = messageDao.findOne(messageId);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if (message == null) {
            return true;
        }

        for (User participant : message.getChat().getParticipants()) {
            if (participant.getName().equals(details.getUsername())) {
                return true;
            }
        }

        return false;
    }
}

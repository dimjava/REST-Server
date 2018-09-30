package com.reshigo.service.chats;

import com.microsoft.azure.storage.StorageException;
import com.reshigo.dao.ChatDao;
import com.reshigo.dao.MessageDao;
import com.reshigo.dao.permanent.MessageTypeDao;
import com.reshigo.exception.NotAllowed;
import com.reshigo.exception.NotFound;
import com.reshigo.model.entity.Chat;
import com.reshigo.model.entity.Message;
import com.reshigo.model.entity.MessageType;
import com.reshigo.model.entity.User;
import com.reshigo.model.entity.permanent.ChatStatusEnum;
import com.reshigo.notifications.MessageReceivedNotification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by dmitry103 on 18/08/17.
 */

@Service
public class ChatsServiceHelper {

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private ChatDao chatDao;

    @Autowired
    private MessageTypeDao messageTypeDao;

    @Transactional(rollbackFor = IOException.class)
    public User addMessage(Long id, Message message, String username, MessageReceivedNotification notification) throws NotFound, NotAllowed, IOException, URISyntaxException, StorageException {
        Chat chat = chatDao.findOne(id);

        if (chat == null) {
            throw new NotFound(null);
        }

        if (!chat.getStatus().equals(ChatStatusEnum.OPEN)) {
            throw new NotAllowed(null);
        }

        MessageType messageType = messageTypeDao.findOne(message.getMessageType().getId());

        message.setDate(new Timestamp(new Date().getTime()));
        message.setChat(chat);
        message.setMessageType(messageType);

        for (User user : chat.getParticipants()) {
            if (user.getName().equals(username)) {
                message.setUser(user);
                messageDao.save(message);

                for (User recipient : chat.getParticipants()) {
                    if (recipient.getName().equals(user.getName())) {
                        continue;
                    }

                    notification.setChatId(chat.getId());
                    notification.setId(message.getId());
                    notification.setMessageType(message.getMessageType().getId());
                    notification.setDate(message.getDate().getTime());
                    notification.setUser(username);
                    notification.setWidth(message.getWidth());
                    notification.setHeight(message.getHeight());

                    if (message.getMessageType().getTitle().equals("text")) {
                        notification.setData(new String(message.getData()));
                    }

                    return recipient;
                }

                break;
            }
        }

        return null;
    }
}

package com.reshigo.service.chats;

import com.microsoft.azure.storage.StorageException;
import com.reshigo.dao.ChatDao;
import com.reshigo.dao.MessageDao;
import com.reshigo.dao.UserDao;
import com.reshigo.dao.permanent.MessageTypeDao;
import com.reshigo.exception.NotAllowed;
import com.reshigo.exception.NotFound;
import com.reshigo.model.entity.Chat;
import com.reshigo.model.entity.Message;
import com.reshigo.model.entity.User;
import com.reshigo.notifications.*;
import org.hibernate.Hibernate;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;

import static com.reshigo.model.entity.permanent.ChatStatusEnum.CLOSED;
import static com.reshigo.model.entity.permanent.ChatStatusEnum.OPEN;
import static com.reshigo.notifications.NotificationTopicEnum.*;

/**
 * Created by dmitry103 on 23/07/16.
 */

@Service
public class ChatsService {

    private Logger logger = LoggerFactory.getLogger(ChatsService.class);

    @Autowired @Qualifier("feedService")
    private NotificationMessagingTemplate notificationMessagingTemplate;

    @Autowired
    private ChatDao chatDao;

    @Autowired
    private MessageDao messageDao;

    @Autowired
    private MessageTypeDao messageTypeDao;

    @Autowired
    private ChatsServiceHelper helper;

    @Autowired
    private UserDao userDao;

    @Transactional
    public List<Message> getMessages(Long date, Long id) throws NotFound {
        Chat chat = chatDao.findOne(id);

        if (chat == null) {
            throw new NotFound(null);
        }

        return messageDao.get(id, date);
    }

    @Transactional
    public Message addMessage(Long id, Message message, String username) throws NotFound, NotAllowed, IOException, IllegalAccessException, URISyntaxException, StorageException {
        MessageReceivedNotification notification = new MessageReceivedNotification();

        User recipient = helper.addMessage(id, message, username, notification);
        notificationMessagingTemplate.send(notification , recipient);

        MessageReceivedNotification notification1 = null;
        try {
            notification1 = Notification.clone(notification);
            notification1.setShow(false);
        } catch (CloneNotSupportedException e) {
            logger.error("Unable to clone notification", e);
        }

        notificationMessagingTemplate.echo(notification1, userDao.findOne(username));

        if (message.getMessageType().getId().equals(0L)) {
            if (checkFraudActivity(new String(message.getData()))) {
                notificationMessagingTemplate.send(new TextNotification(
                                "FRAUD DETECTION", "Login " + username),
                        ADMIN.name());
                notificationMessagingTemplate.send(new TextNotification(
                                "FRAUD DETECTION", "Login: " + username),
                        MODERATOR.name());
            }
        }

        return message.cloneNoData();
    }

    @Transactional
    public List<Chat> getChats(String username, String state) {

        String[] statuses;

        if (state.equals("ALL")) {
            statuses = new String[]{CLOSED.name(), OPEN.name()};
        } else if (state.equals("ACTIVE")) {
            statuses = new String[]{OPEN.name()};
        } else if (state.equals("NON_ACTIVE")) {
            statuses = new String[]{CLOSED.name()};
        } else {
            statuses = new String[]{state};
        }

        List<Chat> chats = chatDao.getChats(username, statuses);
        chats.forEach(chat->Hibernate.initialize(chat.getOrder()));

        return chats;
    }

    @Transactional
    public Message getMessage(Long id, Long messageId) throws NotFound, IllegalAccessException {
        Message m = messageDao.findOne(messageId);

        if (m == null) {
            throw new NotFound(null);
        }

        return m;
    }

    @Transactional
    public List<User> getChatParticipants(Long id) throws NotFound {
        Chat chat = chatDao.findOne(id);

        if (chat == null) {
            throw new NotFound(null);
        }

        Hibernate.initialize(chat.getParticipants());

        return chat.getParticipants();
    }

    @Transactional(readOnly = true)
    public Message getLastRead(Long id, String name) {
        return chatDao.getLastRead(id, name);
    }

    @Transactional
    public void setRead(Long id, Long lastId, String name) throws NotFound {
        chatDao.setRead(id, lastId, name);

        Chat chat = chatDao.findOne(id);

        if (chat == null) {
            throw new NotFound(null);
        }

        for (User participant : chat.getParticipants()) {
            if (!participant.getName().equals(name)) {
                notificationMessagingTemplate.send(new LastReadMessageNotification(id, lastId), participant);
            }
        }
    }

    @Transactional
    public void notifyParticipantsIfNoActivity(Long id) {
        if (id == null) {
            return;
        }

        Chat chat = chatDao.findOne(id);

        if (chat == null || chat.getStatus().equals(CLOSED)) {
            return;
        }

        TextNotification tn2solver = new TextNotification("Не заставляйте заказчика волноваться", "Напишите ему, что все в порядке и Вы работаете над заказом");
        TextNotification tn2customer = new TextNotification("Долго нет вестей от исполнителя ?", "Попробуйте написать в чат, чтобы узнать, все ли в порядке");

        List<String> messageAuthors = (List<String>) chatDao.getSession().createQuery("select distinct m.user.name from Message m where m.chat.id = :id")
                .setParameter("id", id)
                .list();

        Hibernate.initialize(chat.getOrder());

        if (!messageAuthors.contains(chat.getOrder().getSolver().getName())) {
            notificationMessagingTemplate.send(tn2solver, chat.getOrder().getSolver());
        }

        if (!messageAuthors.contains(chat.getOrder().getUser().getName())) {
            notificationMessagingTemplate.send(tn2customer, chat.getOrder().getUser());
        }
    }

    public boolean checkFraudActivity(String text) {
        String[] prohibitedStrings = new String[] {"вконтакте", "телефон", "вотсап", "телеграм", "карта", "на карту", "номер карты"};
        String[] regexps = new String [] {".*vk\\.com.*", ".*id\\d*.*", ".*((8|\\+7)[\\- ]?)?(\\(?\\d{3}\\)?[\\- ]?)?[\\d\\- ]{7,10}.*",
                "(вк)|(^вк$)|(.* вк .*)|(^вк .*)|(.* вк$)"};

        for (String s : prohibitedStrings) {
            if (text.toLowerCase().contains(s)) {
                return true;
            }
        }

        for (String r : regexps) {
            if (Pattern.matches(r, text.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}

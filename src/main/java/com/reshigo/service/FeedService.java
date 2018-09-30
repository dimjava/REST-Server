package com.reshigo.service;

import com.reshigo.model.entity.Topic;
import com.reshigo.model.entity.User;
import com.reshigo.notifications.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by dmitry103 on 28/06/17.
 */

@Service
public class FeedService implements NotificationMessagingTemplate {
    @Autowired @Qualifier("websocketMessagingTemplate")
    private NotificationMessagingTemplate websocketMessagingTemplate;

    @Autowired @Qualifier("FCMNotificationMessagingTemplate")
    private NotificationMessagingTemplate notificationMessagingTemplate;

    @Autowired
    private FeedServiceHelper helper;

    @Override
    public boolean check(String token) {
        return notificationMessagingTemplate.check(token);
    }

    @Async
    @Override
    public void send(Notification o, User user) {
        helper.send(o, user);

        if (o.getText() != null) {
            SimpleDataNotification n = new SimpleDataNotification(o.getTitle(), o.getText(), o.getPushData());
            notificationMessagingTemplate.send(n, user);
        }

        websocketMessagingTemplate.send(new SyncNotification(), user);
    }

    @Async
    @Override
    public void echo(Notification o, User user) {
        helper.send(o, user, true);
        websocketMessagingTemplate.send(new SyncNotification(), user);
    }

    @Async
    @Override
    public void send(Notification o, String topic, String... ignore) {
        for (Topic t: helper.getUsersByTopic(topic)) {
            if (Arrays.asList(ignore).contains(t.getUser().getName())) {
                continue;
            }

            helper.send(o, t.getUser());
            websocketMessagingTemplate.send(new SyncNotification(), t.getUser());
        }

        if (o.getText() != null) {
            SimpleDataNotification n = new SimpleDataNotification(o.getTitle(), o.getText(), o.getPushData());
            notificationMessagingTemplate.send(n, topic);
        }
    }

    @Override
    public void subscribe(User user, String topic) {
        helper.subscribe(user, topic);
        notificationMessagingTemplate.subscribe(user, topic);
    }

    @Override
    public void unsubscribe(User user, String topic) {
        helper.unsubscribe(user, topic);
        notificationMessagingTemplate.unsubscribe(user, topic);
    }

    @Override
    public void delete(User user) {
        notificationMessagingTemplate.delete(user);
    }
}

package com.reshigo.notifications;

import com.reshigo.model.entity.User;
import org.springframework.stereotype.Service;

/**
 * Created by dmitry103 on 04/06/17.
 */

public interface NotificationMessagingTemplate {

    boolean check(String token);

    void send(Notification o, User user);

    void echo(Notification o, User user);

    void send(Notification o, String topic, String... ignore);

    void subscribe(User user, String topic);

    void unsubscribe(User user, String topic);

    void delete(User user);
}

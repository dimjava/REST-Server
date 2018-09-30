package com.reshigo.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reshigo.model.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by dmitry103 on 11/03/17.
 */

@Service
public class WebsocketMessagingTemplate implements NotificationMessagingTemplate {

    private final ObjectMapper objectMapper;

    private final ConcurrentHashMap<String, WebSocketSession> sessions;

    private final ConcurrentSkipListSet<String> solvers;

    @Autowired
    public WebsocketMessagingTemplate(ObjectMapper objectMapper, ConcurrentHashMap<String, WebSocketSession> sessions, ConcurrentSkipListSet<String> solvers) {
        this.objectMapper = objectMapper;
        this.sessions = sessions;
        this.solvers = solvers;
    }

    @Override
    public boolean check(String token) {
        return true;
    }

    @Async
    @Override
    public void send(Notification o, User user) {
        byte[] data;

        try {
            data = objectMapper.writeValueAsString(o).getBytes();
        } catch (JsonProcessingException e) {
            e.printStackTrace();

            return;
        }

        WebSocketMessage<?> message = new BinaryMessage(data);

        if (sessions.get(user.getName()) != null) {
            try {
                sessions.get(user.getName()).sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Async
    @Override
    public void echo(Notification o, User user) {
        send(o, user);
    }

    @Async
    @Override
    public void send(Notification o, String topic, String... ignore) {
        if (!topic.equals(NotificationTopicEnum.SOLVER.name())) {
            return;
        }

        byte[] data;

        try {
            data = objectMapper.writeValueAsString(o).getBytes();
        } catch (JsonProcessingException e) {
            e.printStackTrace();

            return;
        }

        WebSocketMessage<?> message = new BinaryMessage(data);

        for (String username : solvers) {
            if (sessions.get(username) != null) {
                try {
                    sessions.get(username).sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void subscribe(User user, String topic) {}

    @Override
    public void unsubscribe(User user, String topic) {}

    @Override
    public void delete(User user) {
    }
}

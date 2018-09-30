package com.reshigo.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reshigo.exception.RestTemplateErrorHandler;
import com.reshigo.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Properties;

/**
 * Created by dmitry103 on 06/06/17.
 */

@Service
public class FCMNotificationMessagingTemplate implements NotificationMessagingTemplate {

    private Logger logger = LoggerFactory.getLogger(FCMNotificationMessagingTemplate.class);

    private String fcmAuthKey;

    private String fcmUrl;

    private String fcmIidUrl;

    @Autowired
    public FCMNotificationMessagingTemplate(@Qualifier("notificationProperties")Properties properties) {
        fcmAuthKey = properties.getProperty("fcm.auth");
        fcmUrl = properties.getProperty("fcm.url");
        fcmIidUrl = properties.getProperty("fcm.iidUrl");
    }

    @Override
    public boolean check(String token) {
        RestTemplate template = new RestTemplate();
        template.setErrorHandler(new RestTemplateErrorHandler());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "key=" + fcmAuthKey);
        HttpEntity entity = new HttpEntity(headers);

        String queryUrl = fcmIidUrl + "iid/v1/info/" + token;

        ResponseEntity response = template.exchange(queryUrl, HttpMethod.GET, entity, Object.class);

        HttpStatus status = response.getStatusCode();

        logger.debug("Query url to check FCM token: {}, {}", queryUrl, status);

        return status.equals(HttpStatus.OK);
    }

    @Async
    @Override
    public void send(Notification o, User user) {
        send(o, user.getFcmToken(), "");
    }

    @Async
    @Override
    public void echo(Notification o, User user) {
        send(o, user);
    }

    @Async
    @Override
    public void send(Notification o, String topic, String... ignore) {
        send(o, topic, "/topics/");
    }

    @Async
    @Override
    public void subscribe(User user, String topic) {
        RestTemplate template = new RestTemplate();
        template.setErrorHandler(new RestTemplateErrorHandler());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set("Authorization", "key=" + fcmAuthKey);
        HttpEntity entity = new HttpEntity(headers);

        String queryUrl = fcmIidUrl + "iid/v1/info/" + user.getFcmToken() + "/rel/topics/" + topic;

        if (logger.isDebugEnabled()) {
            logger.debug("Query url to subscribe user {} to topic {}: {}", user.getName(), topic, queryUrl);
        }

        template.exchange(queryUrl, HttpMethod.POST, entity, Object.class);
    }

    @Override
    public void unsubscribe(User user, String topic) {
        RestTemplate template = new RestTemplate();
        template.setErrorHandler(new RestTemplateErrorHandler());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set("Authorization", "key=" + fcmAuthKey);
        HttpEntity entity = new HttpEntity(headers);

        String queryUrl = fcmIidUrl + "iid/v1/info/" + user.getFcmToken() + "/rel/topics/" + topic;

        if (logger.isDebugEnabled()) {
            logger.debug("Query url to unsubscribe user {} to topic {}: {}", user.getName(), topic, queryUrl);
        }

        template.exchange(queryUrl, HttpMethod.DELETE, entity, Object.class);
    }

    @Override
    public void delete(User user) {
        /*if (user.getFcmToken() ==  null) {
            return;
        }

        RestTemplate template = new RestTemplate();
        template.setErrorHandler(new RestTemplateErrorHandler());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set("Authorization", "key=" + fcmAuthKey);
        HttpEntity entity = new HttpEntity(headers);

        String queryUrl = fcmIidUrl + "v1/web/iid/" + user.getFcmToken();

        template.exchange(queryUrl, HttpMethod.DELETE, entity, Object.class);*/
    }

    private void send(Notification o, String to, String prefix) {
        RestTemplate template = new RestTemplate();
        template.setErrorHandler(new RestTemplateErrorHandler());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.set("Authorization", "key=" + fcmAuthKey);

        FCMNotification notification = new FCMNotification();
        notification.setTo(prefix + to);
        notification.setContentAvailable(true);

        notification.setNotification(o);
        notification.setData(o.getPushData());

        HttpEntity<Object> entity = new HttpEntity<>(notification, headers);

        if (logger.isDebugEnabled()) {
            ObjectMapper om = new ObjectMapper();

            try {
                logger.debug("Sending notification: {}", om.writeValueAsString(notification));
            } catch (JsonProcessingException ignored) {
                logger.debug("Sending notification: {failed to convert}");
            }
        }

        String ret = template.postForObject(fcmUrl, entity, String.class);

        logger.debug("Result for sending FCM notification: {}", ret);
    }

    public String getFcmAuthKey() {
        return fcmAuthKey;
    }

    public void setFcmAuthKey(String fcmAuthKey) {
        this.fcmAuthKey = fcmAuthKey;
    }

    public String getFcmUrl() {
        return fcmUrl;
    }

    public void setFcmUrl(String fcmUrl) {
        this.fcmUrl = fcmUrl;
    }
}

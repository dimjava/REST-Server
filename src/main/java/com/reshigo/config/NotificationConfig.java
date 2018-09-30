package com.reshigo.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileShare;
import com.reshigo.sms.SmsTemplate;
import com.reshigo.sms.SmsAeroTemplate;
import com.reshigo.sms.TwilioMessagingTemplate;
import com.twilio.Twilio;
import com.twilio.http.TwilioRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Created by dmitry103 on 30/07/16.
 */

@Configuration
@EnableAsync
@EnableWebSocket
@EnableTransactionManagement
@ComponentScan(value = {"com.reshigo.sms", "com.reshigo.notifications"})
public class NotificationConfig implements WebSocketConfigurer {

    private Logger logger = LoggerFactory.getLogger(NotificationConfig.class);

    @Bean
    public ConcurrentHashMap<String, WebSocketSession> getSessions() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public ConcurrentSkipListSet<String> getSolvers() {
        return new ConcurrentSkipListSet<> ();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
        webSocketHandlerRegistry.addHandler(new WebSocketHandler() {
            @Override
            public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
                getSessions().put(webSocketSession.getPrincipal().getName(), webSocketSession);
            }

            @Override
            public void handleMessage(WebSocketSession webSocketSession, WebSocketMessage<?> webSocketMessage) throws Exception {
            }

            @Override
            public void handleTransportError(WebSocketSession webSocketSession, Throwable throwable) throws Exception {
                getSessions().remove(webSocketSession.getPrincipal().getName());
            }

            @Override
            public void afterConnectionClosed(WebSocketSession webSocketSession, CloseStatus closeStatus) throws Exception {
                getSessions().remove(webSocketSession.getPrincipal().getName());
            }

            @Override
            public boolean supportsPartialMessages() {
                return false;
            }
        }, "/websocket").setAllowedOrigins("*");
    }

    @Bean @Autowired
    public TwilioRestClient twilio(@Qualifier(value = "notificationProperties") Properties properties) {
        Twilio.init(properties.getProperty("sms.account.sid"), properties.getProperty("sms.auth.token"));

        return Twilio.getRestClient();
    }

    @Bean(name = "notificationProperties")
    public Properties notificationProperties() throws IOException {
        Properties properties = new Properties();

        InputStream is = getClass().getClassLoader().getResourceAsStream("notifications.properties");
        properties.load(is);

        return properties;
    }

    @Bean
    @Autowired
    public CloudFileShare cloudFileShare(@Qualifier("commonProperties") Properties properties) throws URISyntaxException, InvalidKeyException, StorageException {
        CloudStorageAccount account = CloudStorageAccount.parse("DefaultEndpointsProtocol=https;AccountName=reshigo;AccountKey=8S0pVXP0naJattXCzmA/bg/i7BHVngltwvCC7hBRfcrQwllR2QMjeT+pvWJKMON90HFXiX9PhqF7P9ADRJmNpQ==;EndpointSuffix=core.windows.net");
        CloudFileClient client = account.createCloudFileClient();

        if (properties.getProperty("test").equals("1")) {
            return client.getShareReference("test-data");
        }

        return client.getShareReference("data");
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.configure(MapperFeature.USE_ANNOTATIONS, false);

        return om;
    }
}

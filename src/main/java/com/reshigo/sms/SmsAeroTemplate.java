package com.reshigo.sms;

import com.reshigo.model.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

/**
 * Created by dmitry103 on 20/12/16.
 */

@Qualifier(value = "smsAero")
@Service
public class SmsAeroTemplate implements SmsTemplate {
    private Logger logger = LoggerFactory.getLogger(SmsAeroTemplate.class);

    private final Properties properties;

    @Autowired
    public SmsAeroTemplate(@Qualifier("notificationProperties") Properties properties) {
        this.properties = properties;
    }

    @Override
    public void send(User to, String msg) throws Exception {
        logger.debug("Sending message \"{}\" to user {}", msg, to.getName());

        String[] yotaNumbers = {"+7956", "+7958", "+7991", "+7995", "+7996", "+7999"};
        for (String number : yotaNumbers) {
            if (to.getPhone().startsWith(number)) {
                throw new Exception("Yota numbers are proxibited for SMS Aero");
            }
        }

        if (properties.getProperty("test").equals("1")) {
            logger.warn("Property test is set to 1. Not sending sms message.");
            return;
        }

        String query = "https://gate.smsaero.ru/send/"
                + "?user=ya-xaker103@yandex.ru"
                + "&password=qXMUGuprRxmdwzpeHlUqvOhl9hFH"
                + "&to=" + to.getPhone().substring(1)
                + "&text=" + msg
                + "&type=7"
                + "&from=ReshiGo";

        URL url = new URL(query);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.getResponseMessage();
    }
}

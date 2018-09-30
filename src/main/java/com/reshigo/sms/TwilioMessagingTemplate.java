package com.reshigo.sms;

import com.reshigo.model.entity.User;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Properties;

/**
 * Created by dmitry103 on 03/11/16.
 */

@Qualifier(value = "twilioMessageSender")
@Service
public class TwilioMessagingTemplate implements SmsTemplate {
    private Logger logger = LoggerFactory.getLogger(TwilioMessagingTemplate.class);

    private final Properties properties;

    @Autowired
    public TwilioMessagingTemplate(@Qualifier("notificationProperties") Properties properties) {
        this.properties = properties;
    }

    @Override
    public void send(User to, String msg) {
        logger.debug("Sending message \"{}\" to user {}", msg, to.getName());

        if (properties.getProperty("test").equals("1")) {
            logger.warn("Property test is set to 1. Not sending sms message.");
            return;
        }

        Message.creator(new PhoneNumber(to.getPhone()), "MG6ff4917d276033fad1bedfaa8138d67b", msg).create().getStatus();
    }
}

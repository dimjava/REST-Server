package com.reshigo.security;

import com.reshigo.model.entity.User;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.Properties;
/**
 * Created by dmitry103 on 19/04/17.
 */

@Aspect
@Component("SmsAccess")
public class SmsSecutiryComponent {

    Logger logger = LoggerFactory.getLogger(SmsSecutiryComponent.class);

    private final long MAX_SMS = 30L;

    private Properties notificationProperties;

    @Autowired
    public SmsSecutiryComponent(@Qualifier("notificationProperties") Properties notificationProperties) {
        this.notificationProperties = notificationProperties;
    }

    @Around(value = "execution(* com.reshigo.sms.SmsTemplate.send(..)) && args(to,..)")
    public void sendAllowed(ProceedingJoinPoint joinPoint, User to) throws Throwable {

//        if (inBlackList(to.getPhone())) {
//            return;
//        }

        if (to.getSmsCnt() < MAX_SMS) {
            to.setSmsCnt(to.getSmsCnt() + 1);
            joinPoint.proceed();
        } else {
            logger.warn("Sms counter for user {} reached limit {}", to.getName(), MAX_SMS);
        }
    }

    public void addToBlackList(String phone) {

        if (inBlackList(phone)) {
            return;
        }

        BufferedWriter br;

        try {
            br = new BufferedWriter(new FileWriter(notificationProperties.getProperty("blacklist"), true));
        } catch (IOException e) {
            e.printStackTrace();

            return;
        }

        try {
            br.write(phone + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException ignored){}
        }
    }

    private boolean inBlackList(String to) {
        BufferedReader listReader;

        try {
            listReader = new BufferedReader(new FileReader(notificationProperties.getProperty("blacklist")));
        } catch (FileNotFoundException e) {
            e.printStackTrace();

            return false;
        }

        String phone = null;
        try {
            phone = listReader.readLine();
        } catch (IOException e) {
            try {
                listReader.close();

                return false;
            } catch (IOException ignored) {
                return false;
            }
        }

        do {
            if (to.equals(phone)) {

                try {
                    listReader.close();
                } catch (IOException ignored) {}

                // Do not proceed sending sms

                return true;
            }

            try {
                phone = listReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();

                try {
                    listReader.close();

                    return false;
                } catch (IOException ignored) {
                    return false;
                }
            }
        } while (phone != null);

        try {
            listReader.close();
        } catch (IOException ignored) {}

        return false;
    }
}

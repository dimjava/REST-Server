package com.reshigo.sms;

import com.reshigo.model.entity.User;

/**
 * Created by dmitry103 on 03/11/16.
 */
public interface SmsTemplate {
    void send(User to, String msg) throws Exception;
}

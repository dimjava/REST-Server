package com.reshigo.config;

import com.reshigo.payment.PaymentVerifier;
import com.reshigo.payment.RobokassaPaymentVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by dmitry103 on 24/11/16.
 */

@Configuration
public class PaymentConfig {
    @Bean
    public Properties properties() throws IOException {
        Properties properties = new Properties();

        InputStream is = getClass().getClassLoader().getResourceAsStream("payment.properties");
        properties.load(is);

        return properties;
    }

    @Bean
    public PaymentVerifier paymentVerifier(Properties properties) {

        PaymentVerifier pv = new RobokassaPaymentVerifier();

        if (properties.getProperty("test").equals("1")) {
            pv.setKeys(properties.getProperty("debug1"), properties.getProperty("debug2"), 1);
        } else {
            pv.setKeys(properties.getProperty("release1"), properties.getProperty("release2"), 0);
        }

        return pv;
    }
}

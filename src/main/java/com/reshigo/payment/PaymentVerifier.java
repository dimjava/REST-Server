package com.reshigo.payment;

import com.reshigo.model.entity.Payment;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dmitry103 on 24/11/16.
 */
public interface PaymentVerifier {

    boolean verify(BigDecimal outSum, Long invId, String signatureValue) throws NoSuchAlgorithmException;

    String createHtmlPage(Payment payment) throws NoSuchAlgorithmException;

    void setKeys(String publicKey, String privateKey, int isTest);
}

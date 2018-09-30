package com.reshigo.payment;

import com.reshigo.model.entity.Payment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dmitry103 on 24/11/16.
 */

public class RobokassaPaymentVerifier implements PaymentVerifier {
    private String publicKey = "";
    private String privateKey = "";

    private int isTest = 0;

    @Override
    public boolean verify(BigDecimal outSum, Long invId, String signatureValue) throws NoSuchAlgorithmException {
        String controlString = outSum.toString() + ":" + invId.toString() + ":" + privateKey;
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(controlString.getBytes());
        byte[] bytes = md.digest();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        return signatureValue.equals(sb.toString());
    }

    @Override
    public String createHtmlPage(Payment payment) throws NoSuchAlgorithmException {
        String htmlPage = "https://auth.robokassa.ru/Merchant/Index.aspx?";

        String controlString = "Reshigo:" + payment.getAmount().toString() + ":" + payment.getId().toString() + ":" + publicKey;
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        md.update(controlString.getBytes());
        byte[] bytes = md.digest();

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }

        htmlPage += "MerchantLogin=Reshigo" +
                "&OutSum=" + payment.getAmount().toString() +
                "&InvoiceID=" + payment.getId().toString() +
                "&SignatureValue=" + sb.toString() + "&IsTest=" + String.valueOf(isTest);

        return htmlPage;
    }

    @Override
    public void setKeys(String publicKey, String privateKey, int isTest) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.isTest = isTest;
    }
}

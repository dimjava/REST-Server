package com.reshigo.controller;

import com.reshigo.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dmitry103 on 23/11/16.
 */

@RestController
@RequestMapping(value = "/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<String> commitPayment(@RequestParam(value = "InvId") Long invId,
                                        @RequestParam(value = "SignatureValue") String signatureValue,
                                        @RequestParam(value = "OutSum") BigDecimal outSum) {
        HttpStatus status = HttpStatus.OK;
        String answer = "NO_OK";

        try {
            answer = paymentService.updateFunds(outSum, invId, signatureValue);
        } catch (NoSuchAlgorithmException e) {
            status = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity<>(answer, status);
    }

}

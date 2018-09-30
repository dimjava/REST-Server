package com.reshigo.controller;

import com.reshigo.exception.NotAllowed;
import com.reshigo.exception.NotFound;
import com.reshigo.exception.ParamsError;
import com.reshigo.model.PassRecovery;
import com.reshigo.service.RootService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Created by dmitry103 on 24/11/16.
 */

@RestController
@RequestMapping(value = "/")
public class RootController {
    private Logger logger = LoggerFactory.getLogger(RootController.class);

    @Autowired
    private RootService rootService;

//    @RequestMapping(value = "", method = RequestMethod.GET)
//    public ModelAndView redirectHome() {
//        return new ModelAndView("redirect:http://www.reshigo.ru");
//    }

    @RequestMapping(value = "/password", method = RequestMethod.GET)
    public ResponseEntity passwordRecovery(@RequestParam(value = "name", required = false) String name,
                                           @RequestParam(value = "phone", required = false) String phone) {

        if (name != null) {
            logger.debug("Password recovery proceed for user {}", name);
        } else if (phone != null) {
            logger.debug("Password recovery proceed for phone {}", phone);
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        ResponseEntity response = new ResponseEntity(HttpStatus.OK);

        try {
            rootService.passwordRecovery(name, phone);
        } catch (NotFound notFound) {
            response = new ResponseEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            response = new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotAllowed notAllowed) {
            response = new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        return response;
    }

    @RequestMapping(value = "/password/resend", method = RequestMethod.GET)
    public ResponseEntity resendPasswordRecoveryCode(@RequestParam(value = "name", required = false) String name,
                                                     @RequestParam(value = "phone", required = false) String phone) {
        if (name != null) {
            logger.debug("Resending password recovery code for user {}", name);
        } else if (phone != null) {
            logger.debug("Resending password recovery code for phone {}", phone);
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        ResponseEntity response = new ResponseEntity(HttpStatus.OK);

        try {
            rootService.resendPasswordRecoveryCode(name, phone);
        } catch (NotFound notFound) {
            response = new ResponseEntity(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            response = new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotAllowed notAllowed) {
            response = new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        return response;
    }

    @RequestMapping(value = "/code/validate", method = RequestMethod.POST)
    public ResponseEntity validateCode(@Valid @RequestBody PassRecovery recovery) {
        logger.debug("Processing code validation for password recovery. Name: {}, phone: {}, code: {}",
                recovery.getName(), recovery.getPhone(), recovery.getCode());

        if (recovery.getName() == null && recovery.getPhone() == null) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        ResponseEntity response = new ResponseEntity(HttpStatus.OK);

        try {
            if (!rootService.validateCode(recovery.getCode(), recovery.getName(), recovery.getPhone())) {
                if (recovery.getName() != null) {
                    logger.debug("Code for user {} not valid", recovery.getName());
                } else {
                    logger.debug("Code for phone {} not valid", recovery.getPhone());
                }

                response = new ResponseEntity(HttpStatus.BAD_REQUEST);
            } else {
                if (recovery.getName() != null) {
                    logger.debug("Code for user {} is valid", recovery.getName());
                } else {
                    logger.debug("Code for phone {} is valid", recovery.getPhone());
                }
            }
        } catch (NotFound notFound) {
            response = new ResponseEntity(HttpStatus.NOT_FOUND);
        } catch (NotAllowed notAllowed) {
            response = new ResponseEntity(HttpStatus.FORBIDDEN);
        }

        return response;
    }

    @RequestMapping(value = "/password", method = RequestMethod.PUT)
    public ResponseEntity updatePassword(@Valid @RequestBody PassRecovery recovery) {
        if (recovery.getName() != null) {
            logger.debug("Updating password after recovery for user {}", recovery.getName());
        } else if (recovery.getPhone() != null) {
            logger.debug("Updating password after recovery for phone {}", recovery.getPhone());
        } else {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        ResponseEntity response = new ResponseEntity(HttpStatus.OK);

        try {
            if (!rootService.updatePassword(recovery.getName(), recovery.getPhone(), recovery.getPassword(), recovery.getCode())) {
                response = new ResponseEntity(HttpStatus.BAD_REQUEST);
            }
        } catch (NotFound notFound) {
            response = new ResponseEntity(HttpStatus.NOT_FOUND);
        } catch (NotAllowed notAllowed) {
            response = new ResponseEntity(HttpStatus.FORBIDDEN);
        } catch (ParamsError paramsError) {
            response = new ResponseEntity(HttpStatus.BAD_REQUEST);
        }

        return response;
    }
}

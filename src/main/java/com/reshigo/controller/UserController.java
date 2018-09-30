package com.reshigo.controller;

import com.microsoft.azure.storage.StorageException;
import com.reshigo.exception.NotAllowed;
import com.reshigo.exception.NotAvailable;
import com.reshigo.exception.NotFound;
import com.reshigo.exception.registration.DuplicateEntityError;
import com.reshigo.exception.registration.DuplicatePhoneError;
import com.reshigo.model.HttpResponseEntity;
import com.reshigo.model.Registration;
import com.reshigo.model.entity.Feed;
import com.reshigo.model.entity.Payment;
import com.reshigo.model.entity.Review;
import com.reshigo.model.entity.User;
import com.reshigo.service.UserService;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@RestController
@RequestMapping(value = "/user")
public class UserController {
    private Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;

    //GET requests

    @RequestMapping(value = "/feeds/{id}")
    public ResponseEntity<List<Feed>> getFeeds(@PathVariable(value = "id") Long lastId, HttpServletRequest request) {
        MutableBoolean late = new MutableBoolean(false);

        List<Feed> feeds = userService.getFeeds(lastId, request.getUserPrincipal().getName(), late);

        if (late.booleanValue()) {
            return new ResponseEntity<>(feeds, HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(feeds, HttpStatus.OK);
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public void logout(HttpServletRequest request) {
        userService.logout(request.getUserPrincipal().getName());
    }

    @RequestMapping(value = "", method = RequestMethod.GET)
    @PreAuthorize("@LoginComponent.updateLastVisit(principal)")
    public User user(HttpServletRequest request) {
        return userService.getByName(request.getUserPrincipal().getName());
    }


    @RequestMapping(value = "/funds", method = RequestMethod.GET)
    public ResponseEntity<String> increaseFunds(@RequestParam(value = "amount") Long amount, HttpServletRequest request) {
        HttpStatus status = HttpStatus.OK;
        String paymentPage = "";

        try {
            paymentPage = userService.increaseFunds(amount, request.getUserPrincipal().getName());
        } catch (NotAllowed notAllowed) {
            status = HttpStatus.FORBIDDEN;
        } catch (NoSuchAlgorithmException e) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(paymentPage, status);
    }

    @RequestMapping(value = "/payments", method = RequestMethod.GET)
    public ResponseEntity<List<Payment>> getPayments(HttpServletRequest request) {
        HttpStatus status = HttpStatus.OK;
        List<Payment> payments = userService.getPayments(request.getUserPrincipal().getName());

        return new ResponseEntity<>(payments, status);
    }

    @RequestMapping(value = "/registration/verification/code", method = RequestMethod.GET)
    public ResponseEntity resendCode(@RequestParam(value = "name") String username) {
        HttpStatus status = HttpStatus.OK;

        try {
            userService.resendCode(username);
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
        } catch (NotAvailable notAvailable) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        } catch (NotAllowed notAllowed) {
            status = HttpStatus.FORBIDDEN;
        }

        return new ResponseEntity(status);
    }

    @RequestMapping(value = "/{name}/photo", method = RequestMethod.GET)
    public void getProfilePhoto(@PathVariable(value = "name") String name, HttpServletResponse response) {
        byte[] file;
        try {
            file = userService.getProfilePhoto(name);
        } catch (NotFound notFound) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

            return;
        } catch (IOException|StorageException|URISyntaxException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("image/jpeg");

        InputStream is = new ByteArrayInputStream(file);
        try {
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{name}", method = RequestMethod.GET)
    public ResponseEntity<User> getUserAccount(@PathVariable(value = "name") String name) {
        User user = userService.getByName(name);

        if (user == null) {
            return new ResponseEntity<>(new User(), HttpStatus.NOT_FOUND);
        }

        User res = new User();
        res.setName(user.getName());
        res.setInfo(user.getInfo());
        res.setEducation(user.getEducation());
        res.setRating(user.getRating());

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @RequestMapping(value = "/{name}/reviews", method = RequestMethod.GET)
    public ResponseEntity<List<Review>> getReviews(@PathVariable(value = "name") String solvername,
                                             @RequestParam(value = "offset", defaultValue = "0") Long offset,
                                             @RequestParam(value = "limit", defaultValue = "30") Long limit) {
        List<Review> reviews = userService.getReviews(solvername, offset, limit);

        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

    //POST requests

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public ResponseEntity<HttpResponseEntity> register(@RequestBody Registration registration) {
        User user = new User();
        user.setEmail(registration.getEmail());
        user.setPassword(registration.getPassword());
        user.setName(registration.getName());
        user.setPhone(registration.getPhone());
        user.setCampaign(registration.getCampaign());

        HttpStatus status = HttpStatus.CREATED;
        HttpResponseEntity response = new HttpResponseEntity();

        try {
            userService.register(user);
        } catch (DuplicateEntityError | DuplicatePhoneError duplicateEntityError) {
            status = HttpStatus.CONFLICT;
            response.setError(duplicateEntityError.getError());
        } catch (NotAvailable notAvailable) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
            response.setError(notAvailable.getError());
        } catch (NotAllowed notAllowed) {
            status = HttpStatus.BAD_REQUEST;
            response.setError(notAllowed.getError());
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            response.setError("Ошибка сервера. Пожалуйста, обратитесь в поддержку");
        }

        return new ResponseEntity(response, status);
    }

    @RequestMapping(value = "/promocode", method = RequestMethod.POST)
    public ResponseEntity setPromoCode(@RequestBody String promoCode, HttpServletRequest request) {
        HttpStatus status = HttpStatus.SERVICE_UNAVAILABLE;

        /*try {
            userService.setPromoCode(promoCode, request.getUserPrincipal().getName());
        } catch (NotAllowed notAllowed) {
            status = HttpStatus.FORBIDDEN;
        } catch (NotAvailable notAvailable) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
        } catch (Exception e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }*/

        return new ResponseEntity(status);
    }

    @RequestMapping(value = "/registration/verification", method = RequestMethod.POST)
    public ResponseEntity verify(@RequestBody String code, @RequestParam(value = "name") String username) {
        HttpStatus status = HttpStatus.OK;

        if (!userService.verify(code, username)) {
            status = HttpStatus.FORBIDDEN;
        }

        return new ResponseEntity(status);
    }

    // PUT


    @RequestMapping(value = "/fcm", method = RequestMethod.PUT)
    @PreAuthorize("@LoginComponent.updateLastVisit(principal)")
    public ResponseEntity updateFcmToken(@RequestBody String token, HttpServletRequest request) {
        userService.updateFcm(token, request.getUserPrincipal().getName());

        return new ResponseEntity(HttpStatus.OK);
    }

//    @RequestMapping(value = "/fcm/subscribe/{topic}", method = RequestMethod.PUT)
//    public ResponseEntity subscribe(@PathVariable(value = "topic") String topic, HttpServletRequest request) {
//
//        userService.subscribe(topic, request.getUserPrincipal().getName());
//
//        return new ResponseEntity(HttpStatus.OK);
//    }
//
//    @RequestMapping(value = "/fcm/subscribe/{topic}", method = RequestMethod.DELETE)
//    public ResponseEntity unsubscribe(@PathVariable(value = "topic") String topic, HttpServletRequest request) {
//
//        userService.unsubscribe(topic, request.getUserPrincipal().getName());
//
//        return new ResponseEntity(HttpStatus.OK);
//    }

    //updates only user's Info and Education
    @RequestMapping(value = "", method = RequestMethod.PUT)
    public ResponseEntity updateUserInfo(@RequestBody User user, HttpServletRequest request) {
        userService.updateInfo(user, request.getUserPrincipal().getName());

        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/photo", method = RequestMethod.POST)
    public ResponseEntity updatePhoto(MultipartRequest request, HttpServletRequest httpServletRequest) {

        if (request.getFile("file").getSize() > 50000) {
            return new ResponseEntity(HttpStatus.PAYLOAD_TOO_LARGE);
        }

        try {
            userService.updatePhoto(httpServletRequest.getUserPrincipal().getName(), request.getFile("file").getBytes());
        } catch (IOException e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (StorageException|URISyntaxException e) {
            e.printStackTrace();

            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(HttpStatus.OK);
    }
}

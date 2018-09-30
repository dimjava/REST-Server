package com.reshigo.controller.moderator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reshigo.exception.NotAllowed;
import com.reshigo.exception.NotAvailable;
import com.reshigo.exception.NotFound;
import com.reshigo.exception.registration.DuplicateEntityError;
import com.reshigo.exception.registration.DuplicatePhoneError;
import com.reshigo.model.entity.*;
import com.reshigo.model.entity.permanent.OrderStatusEnum;
import com.reshigo.service.UserService;
import com.reshigo.service.chats.ChatsService;
import com.reshigo.service.moderator.ModeratorService;
import com.reshigo.service.orders.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by dmitry103 on 27/12/16.
 */

@RestController
@RequestMapping(value = "/moderator")
public class ModeratorController {
    private Logger logger = LoggerFactory.getLogger(ModeratorController.class);

    private final ModeratorService moderatorService;

    private final OrdersService ordersService;

    private final ChatsService chatsService;

    private final UserService userService;

    private final ObjectMapper om;

    @Autowired
    public ModeratorController(ModeratorService moderatorService, OrdersService ordersService,
                               ChatsService chatsService, UserService userService, ObjectMapper om) {
        this.moderatorService = moderatorService;
        this.ordersService = ordersService;
        this.chatsService = chatsService;
        this.userService = userService;
        this.om = om;
    }

    @RequestMapping(value = {"", "/Orders", "/Payments", "/Users", "/Feeds"}, method = GET)
    public ModelAndView account() {
        return new ModelAndView("redirect:/moderator/index.html");
    }

//    @CrossOrigin(origins = "http://localhost:4200", allowedHeaders = {"error"})
    @RequestMapping(value = "/users", method = GET)
    public ResponseEntity<Map<String, List<User>>> users(@RequestParam(value = "page") Long page,
                                                         @RequestParam(value = "perPage") Long perPage,
                                                         @RequestParam(value = "nameFilter") String nameFilter,
                                                         @RequestParam(value = "phoneFilter") String phoneFilter) {
        //ModelAndView mv = new ModelAndView("/moderator/users");

        //mv.addObject("users", moderatorService.getUsers(name));

        Map<String, List<User>> result = new LinkedHashMap<>();
        result.put("result", moderatorService.getUsers(page, perPage, nameFilter, phoneFilter));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    //@CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/users/block", method = GET)
    public ResponseEntity<Map<String, String[]>> blockUser(@RequestParam(value = "name") String name) {
        moderatorService.blockUser(name);

        Map<String, String[]> result = new LinkedHashMap<>();
        result.put("result", new String[]{});

        return new ResponseEntity(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/users/{login}/code", method = GET)
    public ResponseEntity<Map<String, String>> getUserCode(@PathVariable(value = "login") String login) {
        String code = String.valueOf(this.userService.getByName(login).getCode());

        Map<String, String> result = new LinkedHashMap<>();
        result.put("result", code);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    //@CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value = "/orders", method = GET)
    public ResponseEntity<Map<String, List<Order>>> orders(
            @RequestParam(value = "page") Long page,
            @RequestParam(value = "perPage") Long perPage,
            @RequestParam(value = "nameFilter") String nameFilter,
            @RequestParam(value = "solverNameFilter") String solverNameFilter,
            @RequestParam(value = "status") String status) {

        Map<String, List<Order>> result = new LinkedHashMap<>();
        result.put("result", moderatorService.getOrders(page, perPage, nameFilter, solverNameFilter, status));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    //@CrossOrigin(origins = "http://localhost:4200", methods = {RequestMethod.PUT})
    @RequestMapping(value = "/orders/{id}", method = GET)
    public ResponseEntity<Map<String, String[]>> updateStatus(@PathVariable(value = "id") Long id,
                                                              @RequestParam(value = "status") OrderStatusEnum status) {

        HttpStatus httpStatus = HttpStatus.OK;

        try {
            moderatorService.updateStatus(id, status);
        } catch (NotFound notFound) {
            httpStatus = HttpStatus.NOT_FOUND;
        } catch (NotAllowed notAllowed) {
            httpStatus = HttpStatus.FORBIDDEN;
        }

        Map<String, String[]> result = new LinkedHashMap<>();
        result.put("result", new String[]{});

        return new ResponseEntity(result, httpStatus);
    }

    @RequestMapping(value = "/orders/{id}/pictures/{pictureId}", method = GET)
    public void getPicture(@PathVariable("id") Long id,
                           @PathVariable("pictureId") Long pictureId,
                           HttpServletResponse response) {
        byte[] file = null;

        try {
            file = ordersService.getOrderPictureAsFile(id, pictureId);
        } catch (com.reshigo.exception.NotFound notFound) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

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

    @RequestMapping(value = "/chats/{id}", method = GET)
    public ResponseEntity<Map<String, List<Message>>> getMessages(@PathVariable("id") Long id) {
        HttpStatus status = HttpStatus.OK;
        Map<String, List<Message>> result = new LinkedHashMap<>();

        try {
            result.put("result", chatsService.getMessages(0L, id));
        } catch (com.reshigo.exception.NotFound notFound) {
            result.put("result", new LinkedList<>());
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(result, status);
    }

    @RequestMapping(value = "/chats/messages/{message_id}/v2", method = GET)
    public void getMessageAsFile(@PathVariable(value = "message_id") Long messageId,
                                 HttpServletResponse response) {
        HttpStatus status = HttpStatus.OK;

        Message m = new Message();

        try {
            m = chatsService.getMessage(null, messageId);
        } catch (com.reshigo.exception.NotFound notFound) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

            return;
        } catch (IllegalAccessException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            return;
        }

        if (m == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        response.setContentType("image/jpeg");
        InputStream is = new ByteArrayInputStream(m.getData());
        try {
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/payments", method = GET)
    public ResponseEntity<Map<String, List<Payment>>> payments(@RequestParam(value = "page") Long page,
                                                               @RequestParam(value = "perPage") Long perPage,
                                                               @RequestParam(value = "nameFilter") String nameFilter) {
        HttpStatus httpStatus = HttpStatus.OK;

        Map<String, List<Payment>> result = new LinkedHashMap<>();
        result.put("result", moderatorService.getPayments(page, perPage, nameFilter));

        return new ResponseEntity<>(result, httpStatus);
    }

    @RequestMapping(value = "/feeds", method = GET)
    public ResponseEntity<Map<String, List<Feed>>> feeds(@RequestParam(value = "page") Long page,
                                                                 @RequestParam(value = "perPage") Long perPage,
                                                                 @RequestParam(value = "nameFilter") String nameFilter) {
        HttpStatus httpStatus = HttpStatus.OK;

        Map<String, List<Feed>> result = new LinkedHashMap<>();
        result.put("result", moderatorService.getFeeds(page, perPage, nameFilter));

        return new ResponseEntity<>(result, httpStatus);
    }

    //@CrossOrigin(origins = "http://localhost:4200", methods = {RequestMethod.POST, RequestMethod.OPTIONS})
    @RequestMapping(value = "/notify", method = RequestMethod.POST)
    public ResponseEntity notifyUsers(@RequestBody String str) throws UnsupportedEncodingException {

        try {
            moderatorService.createFeed(str);
        } catch (IOException e) {
            return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/registration/solver", method = RequestMethod.POST)
    public ResponseEntity<String> registerSolver(@RequestBody String userStr) {

        User user = null;
        try {
            user = om.readValue(userStr, User.class);
        } catch (IOException e) {
            return new ResponseEntity<>("Can't parse the message.", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Solver registration proceed: {}, {}, {}, {}, {}",
                    user.getName(), user.getPhone(), user.getRating(),
                    user.getEducation(), user.getInfo());
        }

        try {
            userService.registerSolver(user);
        } catch (DuplicateEntityError | DuplicatePhoneError duplicateEntityError) {
            return new ResponseEntity<>(duplicateEntityError.getError(), HttpStatus.BAD_REQUEST);
        } catch (NotAvailable notAvailable) {
            return new ResponseEntity<>(notAvailable.getError(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (NotAllowed notAllowed) {
            return new ResponseEntity<>(notAllowed.getError(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal server error. Contact support", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("successfully created", HttpStatus.OK);
    }

    @RequestMapping(value = "/funds/release", method = GET)
    public ResponseEntity<String> releaseFunds(@RequestParam(value = "name") String name,
                                               @RequestParam(value = "amount") Double amount,
                                               @RequestParam(value = "commission", required = false) String commission,
                                               @RequestParam(value = "comment") String comment) {
        try {
            moderatorService.releaseFunds(name, amount, commission, java.net.URLDecoder.decode(comment, "UTF-8"));
        } catch (NotFound notFound) {
            return new ResponseEntity<>(notFound.getError(), HttpStatus.NOT_FOUND);
        } catch (NotAllowed notAllowed) {
            return new ResponseEntity<>(notAllowed.getError(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("OK", HttpStatus.OK);
    }

    @RequestMapping(value = "/orders/{id}/review", method = RequestMethod.DELETE)
    public ResponseEntity actOnReview(@PathVariable(value = "id") Long id) {
        moderatorService.actOnReview(id);

        return new ResponseEntity("OK", HttpStatus.OK);
    }

    @RequestMapping(value = "/orders/{id}/price/{price}", method = RequestMethod.GET)
    public ResponseEntity updatePrice(@PathVariable(value = "id") Long id,
                                      @PathVariable(value = "price") Long price) {
        try {
            moderatorService.updatePrice(id, price);
        } catch (NotFound notFound) {
            return new ResponseEntity(notFound.getError(), HttpStatus.NOT_FOUND);
        } catch (NotAllowed notAllowed) {
            return new ResponseEntity(notAllowed.getError(), HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity("OK", HttpStatus.OK);
    }

    @RequestMapping(value = "/report", method = GET)
    public ResponseEntity<Map<String, BigDecimal[]>> getReport(@RequestParam(value = "from") Long from,
                                                               @RequestParam(value = "until") Long until) {
        Map<String, BigDecimal[]> result = new LinkedHashMap<>();
        result.put("result", moderatorService.getFundsStatistics(new Date(from), new Date(until)));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/report/funds/check", method = GET)
    public ResponseEntity<Map<String, List<String>>> getFundsConsistencyCheck() {
        Map<String, List<String>> result = new LinkedHashMap<>();
        result.put("result", moderatorService.fundsConsistencyCheck());
        HttpStatus status = HttpStatus.OK;

        return new ResponseEntity<>(result, status);
    }
}

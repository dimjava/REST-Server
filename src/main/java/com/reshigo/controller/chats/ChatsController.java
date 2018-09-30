package com.reshigo.controller.chats;

import com.microsoft.azure.storage.StorageException;
import com.reshigo.exception.NotAllowed;
import com.reshigo.exception.NotFound;
import com.reshigo.model.entity.Chat;
import com.reshigo.model.entity.Message;
import com.reshigo.model.entity.MessageType;
import com.reshigo.model.entity.User;
import com.reshigo.model.entity.permanent.MessageTypeEnum;
import com.reshigo.service.chats.ChatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by dmitry103 on 23/07/16.
 */

@RestController
@RequestMapping(value = "/chats")
public class ChatsController {
    @Autowired
    private ChatsService chatsService;

    //GET

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity< List<Chat> > getChats(@RequestParam(value = "state", defaultValue = "ALL") String state,
                                                 HttpServletRequest request) {
        List<Chat> chats = chatsService.getChats(request.getUserPrincipal().getName(), state);
        HttpStatus httpStatus = HttpStatus.OK;

        return new ResponseEntity<>(chats, httpStatus);
    }

    @RequestMapping(value = "/{id}/messages", method = RequestMethod.GET)
    @PreAuthorize("@ChatAccess.isAllowed(principal, #id)")
    public ResponseEntity< List<Message> > getMessages(@RequestParam(value = "date", defaultValue = "0") Long date,
                                      @PathVariable(value = "id") Long id) {
        List<Message> messages = new LinkedList<>();
        HttpStatus status = HttpStatus.OK;

        try {
            messages = chatsService.getMessages(date, id);
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(messages, status);
    }

    @RequestMapping(value = "/{id}/messages/{message_id}", method = RequestMethod.GET)
    @PreAuthorize("@ChatAccess.isMessageAllowed(principal, #messageId)")
    public ResponseEntity<Message> getMessage(@PathVariable(value = "id") Long id,
                                              @PathVariable(value = "message_id") Long messageId) {
        HttpStatus status = HttpStatus.OK;

        Message m = new Message();

        try {
            m = chatsService.getMessage(id, messageId);
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
        } catch (IllegalAccessException e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(m, status);
    }

    @RequestMapping(value = "/{id}/messages/{message_id}/v2", method = RequestMethod.GET)
    @PreAuthorize("@ChatAccess.isMessageAllowed(principal, #messageId)")
    public void getMessageAsFile(@PathVariable(value = "id") Long id,
                                 @PathVariable(value = "message_id") Long messageId,
                                 HttpServletResponse response) {
        HttpStatus status = HttpStatus.OK;

        Message m = new Message();

        try {
            m = chatsService.getMessage(id, messageId);
        } catch (NotFound notFound) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } catch (IllegalAccessException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        response.setContentType("image/jpeg");
        InputStream is = new ByteArrayInputStream(m.getData());
        try {
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());
        } catch (IOException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "/{id}/participants", method = RequestMethod.GET)
    @PreAuthorize("@ChatAccess.isAllowed(principal, #id)")
    public ResponseEntity< List<User> >  getParticipants(@PathVariable(value = "id") Long id) {
        HttpStatus status = HttpStatus.OK;

        List<User> participants = new LinkedList<>();

        try {
            participants = chatsService.getChatParticipants(id);
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
        }

        //Secure private data
        for (User participant : participants) {
            participant.setPhone(null);
            participant.setEmail(null);
        }

        return new ResponseEntity<>(participants, status);
    }

    @RequestMapping(value = "/{id}/messages/read", method = RequestMethod.GET)
    public ResponseEntity<Message> getLastRead(@PathVariable(value = "id") Long id, HttpServletRequest request) {
        Message message = chatsService.getLastRead(id, request.getUserPrincipal().getName());

        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    //PUT

    @RequestMapping(value = "/{id}/messages/read", method = RequestMethod.PUT)
    public void setRead(@PathVariable(value = "id") Long id, @RequestParam(value = "lastId") Long lastId, HttpServletRequest request) {
        try {
            chatsService.setRead(id, lastId, request.getUserPrincipal().getName());
        } catch (NotFound ignored) {

        }
    }

    //POST

    @RequestMapping(value = "/{id}/messages", method = RequestMethod.POST)
    @PreAuthorize("@ChatAccess.isAllowed(principal, #id)")
    public ResponseEntity<Message> addMessage(@PathVariable(value = "id") Long id, @RequestBody Message message, HttpServletRequest request) {
        HttpStatus status = HttpStatus.CREATED;

        Message m = new Message();

        try {
            m = chatsService.addMessage(id, message, request.getUserPrincipal().getName());
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
        } catch (NotAllowed notAllowed) {
            status = HttpStatus.FORBIDDEN;
        } catch (IOException | IllegalAccessException e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (StorageException|URISyntaxException e) {
            e.printStackTrace();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity(m, status);
    }

    @RequestMapping(value = "/{id}/messages/v2",  method = RequestMethod.POST)
    @PreAuthorize("@ChatAccess.isAllowed(principal, #id)")
    public ResponseEntity<Message> addPicture(@PathVariable(value = "id") Long id,
                                              @RequestParam(value = "height", defaultValue = "480", required = false) Long height,
                                              @RequestParam(value = "width", defaultValue = "320", required = false) Long width,
                                              MultipartRequest request,
                                              HttpServletRequest servletRequest) {
        HttpStatus status = HttpStatus.CREATED;

        Message m = new Message();
        try {
            m.setHeight(height);
            m.setWidth(width);
            m.setData(request.getFile("file").getBytes());
            m.setMessageType(new MessageType());
            m.getMessageType().setId((long) MessageTypeEnum.IMG.ordinal());
        } catch (IOException e) {
            m.setData(null);

            return new ResponseEntity<>(m, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try {
            m = chatsService.addMessage(id, m, servletRequest.getUserPrincipal().getName());
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
        } catch (NotAllowed notAllowed) {
            status = HttpStatus.FORBIDDEN;
        } catch (IOException | IllegalAccessException e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (StorageException|URISyntaxException e) {
            e.printStackTrace();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(m, status);
    }
}

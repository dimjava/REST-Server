package com.reshigo.controller.orders;

import com.reshigo.exception.NotAllowed;
import com.reshigo.exception.NotAvailable;
import com.reshigo.exception.NotFound;
import com.reshigo.model.entity.Order;
import com.reshigo.model.entity.Picture;
import com.reshigo.service.orders.OrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

@RestController
@RequestMapping(value = "/orders")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @RequestMapping(value = "", method = RequestMethod.GET)
    public ResponseEntity<List<Order>> getAvailableOrders(@RequestParam(value = "offsetDate", defaultValue = "-1") Long offsetDate,
                                                          @RequestParam(value = "offset", defaultValue = "0") Long offset,
                                                          @RequestParam(value = "limit", defaultValue = "30") Long limit,
                                                          @RequestParam(value = "subject", defaultValue = "-1") Long subjectId,
                                                          HttpServletRequest request) {

        if (offset < 0 || limit <= 0) {
            return new ResponseEntity<>(new LinkedList<>(), HttpStatus.BAD_REQUEST);
        }

        if (offsetDate == null || offsetDate < 0) {
            offsetDate = 2000000000000L;
        }

        List<Order> orders = ordersService.getAvailableOrders(request.getUserPrincipal().getName(), offsetDate, offset, limit, subjectId);

        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Order> getAvailableOrder(@PathVariable(value = "id") Long id) {
        Order order = null;
        HttpStatus status = HttpStatus.OK;

        try {
            order = ordersService.getAvailableOrder(id);
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(order, status);
    }


    @RequestMapping(value = "/{id}/pictures", method = RequestMethod.GET)
    @PreAuthorize("@OrderAccess.isAllowed(principal, #orderId)")
    public ResponseEntity< List<Picture> > getOrderPictures(@PathVariable(value = "id") Long orderId) {
        HttpStatus status = HttpStatus.OK;
        List<Picture> pictures = new LinkedList<>();

        try {
            pictures = ordersService.getOrderPicturesIds(orderId);
        } catch (NotAllowed notAllowed) {
            status = HttpStatus.FORBIDDEN;
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(pictures, status);
    }

    @RequestMapping(value = "/{id}/pictures/{picture_id}", method = RequestMethod.GET)
    @PreAuthorize("@OrderAccess.isAllowed(principal, #orderId)")
    public ResponseEntity<Picture> getOrderPicture(@PathVariable(value = "id") Long orderId,
                                                   @PathVariable(value = "picture_id") Long pictureId) {
        HttpStatus status = HttpStatus.OK;
        Picture picture = new Picture();

        try {
            picture = ordersService.getOrderPicture(orderId, pictureId);
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(picture, status);
    }

    @RequestMapping(value = "/{id}/pictures/{picture_id}/v2", method = RequestMethod.GET)
    @PreAuthorize("@OrderAccess.isAllowed(principal, #orderId)")
    public void getOrderPictureAsFile(@PathVariable(value = "id") Long orderId,
                                                         @PathVariable(value = "picture_id") Long pictureId,
                                                         HttpServletResponse response) {
        byte[] file = null;

        try {
            file = ordersService.getOrderPictureAsFile(orderId, pictureId);
        } catch (NotFound notFound) {
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


    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public ResponseEntity reserveOrder(@PathVariable(value = "id") Long orderId, HttpServletRequest request) {
        try {
            ordersService.reserve(orderId, request.getUserPrincipal().getName());
            return new ResponseEntity(HttpStatus.OK);
        } catch (NotAllowed e) {
            return new ResponseEntity(HttpStatus.METHOD_NOT_ALLOWED);
        } catch (NotAvailable notAvailable) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        } catch (NotFound notFound) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @RequestMapping(value = "/{id}/suggest", method = RequestMethod.POST)
    public ResponseEntity suggestPrice(@PathVariable(value = "id") Long orderId,
                                       @RequestParam(value = "price") Long price, HttpServletRequest request,
                                       @RequestBody(required = false) String comment) {
        ResponseEntity response = new ResponseEntity(HttpStatus.OK);

        try {
            ordersService.suggestPrice(orderId, price, comment, request.getUserPrincipal().getName());
        } catch (NotFound notFound) {
            response = new ResponseEntity(HttpStatus.NOT_FOUND);
        } catch (NotAvailable notAvailable) {
            response = new ResponseEntity("Order is not AVAILABLE any more", HttpStatus.FORBIDDEN);
        } catch (NotAllowed notAllowed) {
            response = new ResponseEntity("Price is less than 80", HttpStatus.METHOD_NOT_ALLOWED);
        } catch (Exception e) {
            response = new ResponseEntity("Unable to save suggestion", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return response;
    }
}

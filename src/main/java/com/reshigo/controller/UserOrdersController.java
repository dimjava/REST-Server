package com.reshigo.controller;

import com.microsoft.azure.storage.StorageException;
import com.reshigo.exception.*;
import com.reshigo.model.HttpResponseEntity;
import com.reshigo.model.entity.Order;
import com.reshigo.model.entity.Picture;
import com.reshigo.model.entity.PriceSuggest;
import com.reshigo.model.entity.Review;
import com.reshigo.service.UserOrdersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/user/orders")
public class UserOrdersController {
    private final UserOrdersService ordersService;

    @Autowired
    public UserOrdersController(UserOrdersService ordersService) {
        this.ordersService = ordersService;
    }

    //GET requests

    @RequestMapping(value = "", method = RequestMethod.GET)
    public List<Order> getUserOrders(@RequestParam(value = "offset", defaultValue = "0") Long offset,
                                     @RequestParam(value = "limit", defaultValue = "30") Long limit,
                                     @RequestParam(value = "status", defaultValue = "AVAILABLE") String status,
                                     HttpServletRequest request) {

        return ordersService.getAllOrders(offset, limit, status, request.getUserPrincipal().getName());
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    @PreAuthorize("@OrderAccess.isUserAllowed(principal, #id) || @OrderAccess.isSolverAllowed(principal, #id)")
    public ResponseEntity<Order> getOrder(@PathVariable("id") Long id) {
        Order order = new Order();
        HttpStatus status = HttpStatus.OK;
        try {
            order = ordersService.getOrder(id);
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
        } catch (NotAvailable notAvailable) {
            status = HttpStatus.BAD_REQUEST;
        }

        return new ResponseEntity<>(order, status);
    }

    @Deprecated
    @RequestMapping(value = "/{id}/pictures/{picture_id}", method = RequestMethod.GET)
    @PreAuthorize("@OrderAccess.isUserAllowed(principal, #id) || @OrderAccess.isSolverAllowed(principal, #id)")
    public ResponseEntity<Picture> getOrderPicture(@PathVariable("id") Long id,
                                                   @PathVariable("picture_id") Long pictureId) {
        Picture picture = new Picture();
        HttpStatus status = HttpStatus.OK;

        try {
            picture = ordersService.getOrderPicture(id, pictureId);
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity<>(picture, status);
    }

    @RequestMapping(value = "/{id}/suggestions", method = RequestMethod.GET)
    @PreAuthorize("@OrderAccess.isUserAllowed(principal, #id)")
    public ResponseEntity<List<PriceSuggest>> getPriceSuggestions(@PathVariable("id") Long id) {
        return new ResponseEntity<>(ordersService.getSuggestions(id), HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/pictures/{picture_id}/v2", method = RequestMethod.GET)
    @PreAuthorize("@OrderAccess.isUserAllowed(principal, #id) || @OrderAccess.isSolverAllowed(principal, #id)")
    public void getOrderPictureAsFile(@PathVariable("id") Long id,
                                      @PathVariable("picture_id") Long pictureId,
                                      HttpServletResponse response) {
        byte[] file;

        try {
            file = ordersService.getOrderPictureAsFile(id, pictureId);
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

    //POST requests

    @RequestMapping(value = "", method = RequestMethod.POST)
    @PreAuthorize("@OrderAccess.isUserAllowed(principal, #order.getId())")
    public ResponseEntity<Order> postOrder(@Valid @RequestBody Order order) {
        HttpStatus status = HttpStatus.CREATED;
        Order ret = new Order();

        try {
             ret = ordersService.sendOrderToAvailable(order);
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
            ret.setError(notFound.getError());
        } catch (NotAvailable notAvailable) {
            status = HttpStatus.BAD_REQUEST;
            ret.setError(notAvailable.getError());
        } catch (NotAllowed notAllowed) {
            status = HttpStatus.FORBIDDEN;
            ret.setError(notAllowed.getError());
        } catch (ParamsError paramsError) {
            status = HttpStatus.BAD_REQUEST;
            ret.setError(paramsError.getError());
        }

        return new ResponseEntity<>(ret, status);
    }

    @RequestMapping(value = "/{id}/reject", method = RequestMethod.POST)
    @PreAuthorize("@OrderAccess.isUserAllowed(principal, #orderId)")
    public ResponseEntity reject(@PathVariable(value = "id") Long orderId, HttpServletRequest request) {
        try {
            ordersService.rejectOrder(orderId);
        } catch (NotAvailable notAvailable) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } catch (NotFound notFound) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/{id}/done", method = RequestMethod.POST)
    @PreAuthorize("@OrderAccess.isUserAllowed(principal, #orderId)")
    public ResponseEntity accept(@PathVariable(value = "id") Long orderId, HttpServletRequest request) {
        try {
            ordersService.closeOrderDone(orderId);
        } catch (NotAvailable notAvailable) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } catch (NotFound notFound) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/{id}/solve", method = RequestMethod.POST)
    @PreAuthorize("@OrderAccess.isSolverAllowed(principal, #orderId)")
    public ResponseEntity solve(@PathVariable(value = "id") Long orderId, HttpServletRequest request) {
        try {
            ordersService.closeOrderSolved(orderId);
        } catch (NotAvailable notAvailable) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } catch (NotFound notFound) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/{id}/appeal", method = RequestMethod.POST)
    @PreAuthorize("@OrderAccess.isSolverAllowed(principal, #orderId)")
    public ResponseEntity appeal(@PathVariable(value = "id") Long orderId, HttpServletRequest request) {
        try {
            ordersService.appeal(orderId);
        } catch (NotAvailable notAvailable) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } catch (NotFound notFound) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/{id}/confirm", method = RequestMethod.POST)
    @PreAuthorize("@OrderAccess.isSolverAllowed(principal, #orderId)")
    public ResponseEntity confirm(@PathVariable(value = "id") Long orderId, HttpServletRequest request) {
        try {
            ordersService.confirmRejected(orderId);
        } catch (NotAvailable notAvailable) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        } catch (NotFound notFound) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity(HttpStatus.ACCEPTED);
    }

    @RequestMapping(value = "/{id}/pictures", method = RequestMethod.POST)
    @PreAuthorize("@OrderAccess.isUserAllowed(principal, #orderId)")
    public ResponseEntity<Picture> addOrderPicture(@PathVariable(value = "id") Long orderId,
                                                   @RequestBody @Valid Picture picture) {
        Picture res = new Picture();
        HttpStatus status = HttpStatus.CREATED;

        try {
            res = ordersService.addOrderPicture(picture, orderId);
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
        } catch (NotAvailable notAvailable) {
            status = HttpStatus.BAD_REQUEST;
        } catch (IOException e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (StorageException |URISyntaxException e) {
            e.printStackTrace();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(res, status);
    }

    @RequestMapping(value = "/{id}/pictures/v2", method = RequestMethod.POST)
    @PreAuthorize("@OrderAccess.isUserAllowed(principal, #orderId)")
    public ResponseEntity<Picture> addOrderPicture(@PathVariable(value = "id") Long orderId,
                                                   @RequestParam(value = "counter") Long counter,
                                                   MultipartRequest request) {
        Picture res = new Picture();
        HttpStatus status = HttpStatus.CREATED;

        try {
            res = ordersService.addOrderPicture(orderId, counter, request.getFile("file").getBytes());
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
        } catch (NotAvailable notAvailable) {
            status = HttpStatus.BAD_REQUEST;
        } catch (IOException e) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        } catch (StorageException|URISyntaxException e) {
            e.printStackTrace();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(res, status);
    }

    @RequestMapping(value = "/{id}/review",  method = RequestMethod.POST)
    @PreAuthorize("@OrderAccess.isUserAllowed(principal, #orderId)")
    public ResponseEntity<HttpResponseEntity> addReview(@PathVariable(value = "id") Long orderId, @RequestBody @Valid Review review) {
        HttpStatus status = HttpStatus.CREATED;
        HttpResponseEntity response = new HttpResponseEntity();

        try {
            ordersService.addReview(orderId, review);
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
            response.setError(notFound.getError());
        } catch (NotAllowed notAllowed) {
            status = HttpStatus.METHOD_NOT_ALLOWED;
            response.setError(notAllowed.getError());
        }

        return new ResponseEntity<>(response, status);
    }

    //PUT

    @RequestMapping(value = "/{id}/maturity", method = RequestMethod.PUT)
    @PreAuthorize("@OrderAccess.isUserAllowed(principal, #orderId)")
    public ResponseEntity updateMatyrityDate(@PathVariable(value = "id") Long orderId,
                                             @RequestParam(value = "diff") Long tm) {
        HttpStatus status = HttpStatus.OK;
        try {
            ordersService.updateMaturityDate(orderId, tm);
        } catch (NotAllowed notAllowed) {
            status = HttpStatus.BAD_REQUEST;
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
        }

        return new ResponseEntity(status);
    }

    @RequestMapping(value = "/{id}/suggest", method = RequestMethod.PUT)
    @PreAuthorize("@OrderAccess.isUserAllowed(principal, #orderId)")
    public ResponseEntity<HttpResponseEntity> confirmPriceSuggest(@PathVariable(value = "id") Long orderId,
                                                                  @RequestParam(value = "suggest_id") Long suggestId) {
        HttpStatus status = HttpStatus.OK;
        HttpResponseEntity response = new HttpResponseEntity();

        try {
            ordersService.confirmPriceSuggest(orderId, suggestId);
        } catch (NotAllowed notAllowed) {
            status = HttpStatus.METHOD_NOT_ALLOWED;
            response.setError(notAllowed.getError());
        } catch (NotFound notFound) {
            status = HttpStatus.NOT_FOUND;
            response.setError(notFound.getError());
        } catch (NotAvailable notAvailable) {
            status = HttpStatus.FORBIDDEN;
            response.setError(notAvailable.getError());
        } catch (ParamsError paramsError) {
            status = HttpStatus.BAD_REQUEST;
            response.setError(paramsError.getError());
        } catch (FundsError fundsError) {
            status = HttpStatus.NOT_ACCEPTABLE;
            response.setError(fundsError.getError());
        }

        return new ResponseEntity(response, status);
    }

    //DELETE

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    @PreAuthorize("@OrderAccess.isUserAllowed(principal, #orderId)")
    public ResponseEntity deleteOrder(@PathVariable(value = "id") Long orderId) {
        try {
            ordersService.deleteOrder(orderId);
        } catch (NotAllowed notAllowed) {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        } catch (NotFound notFound) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/pictures/{picture_id}", method = RequestMethod.DELETE)
    @PreAuthorize("@OrderAccess.isUserAllowed(principal, #orderId)")
    public ResponseEntity deleteOrderPicture(@PathVariable(value = "id") Long orderId,
                                             @PathVariable(value = "picture_id") Long pictureId) {
        try {
            ordersService.deleteOrderPicture(orderId, pictureId);
        } catch (NotFound notFound) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        } catch (NotAllowed notAllowed) {
            return new ResponseEntity(HttpStatus.METHOD_NOT_ALLOWED);
        }

        return new ResponseEntity(HttpStatus.OK);
    }
}

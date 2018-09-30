package com.reshigo.exception;

/**
 * Created by dmitry on 29/06/16.
 */
public class NotFound extends HttpResponseError {
    public NotFound(String error) {
        super(error);
    }
}

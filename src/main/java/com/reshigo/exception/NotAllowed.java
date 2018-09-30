package com.reshigo.exception;

public class NotAllowed extends HttpResponseError {
    public NotAllowed(String error) {
        super(error);
    }
}

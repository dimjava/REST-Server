package com.reshigo.exception;

public class NotAvailable extends HttpResponseError {
    public NotAvailable(String error) {
        super(error);
    }
}

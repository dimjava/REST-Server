package com.reshigo.exception.registration;

import com.reshigo.exception.HttpResponseError;

public class DuplicatePhoneError extends HttpResponseError {
    public DuplicatePhoneError(String error) {
        super(error);
    }
}

package com.reshigo.exception.registration;

import com.reshigo.exception.HttpResponseError;

public class DuplicateEntityError extends HttpResponseError {
    public DuplicateEntityError(String error) {
        super(error);
    }
}

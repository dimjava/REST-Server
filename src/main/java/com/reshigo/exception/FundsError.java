package com.reshigo.exception;

/**
 * Created by dmitry103 on 25/08/17.
 */
public class FundsError extends HttpResponseError {
    public FundsError(String error) {
        super(error);
    }
}

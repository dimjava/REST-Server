package com.reshigo.exception;

/**
 * Created by dmitry103 on 13/04/17.
 */
public class ParamsError extends HttpResponseError {
    public ParamsError(String error) {
        super(error);
    }
}

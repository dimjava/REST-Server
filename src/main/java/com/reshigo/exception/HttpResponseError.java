package com.reshigo.exception;

/**
 * Created by dmitry103 on 30/12/2017.
 */
public class HttpResponseError extends Throwable {
    private String error;

    public HttpResponseError(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}

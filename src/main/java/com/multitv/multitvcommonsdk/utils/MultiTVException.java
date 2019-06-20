package com.multitv.multitvcommonsdk.utils;

/**
 * Created by root on 9/4/16.
 */
public class MultiTVException extends Exception {

    private String message = null;

    public MultiTVException() {
        super();
    }

    public MultiTVException(String message) {
        super(message);
        this.message = message;
    }

    public MultiTVException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        return message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

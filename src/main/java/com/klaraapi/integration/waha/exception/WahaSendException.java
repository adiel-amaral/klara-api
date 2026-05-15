package com.klaraapi.integration.waha.exception;

public class WahaSendException extends RuntimeException {

    public WahaSendException(String message) {
        super(message);
    }

    public WahaSendException(String message, Throwable cause) {
        super(message, cause);
    }
}

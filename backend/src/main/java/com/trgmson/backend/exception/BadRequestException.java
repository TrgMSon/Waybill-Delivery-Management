package com.trgmson.backend.exception;

/**
 * BadRequestException
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message)
    {
        super(message);
    }
}

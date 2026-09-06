package com.omar.loomdemo.exception;

/**
 * Signals a failure while orchestrating a loan application, such as an
 * interrupted concurrent fetch of customer data or a failure reported by
 * banking-data-service.
 */
public class LoanOriginationException extends RuntimeException {

    public LoanOriginationException(String message) {
        super(message);
    }

    public LoanOriginationException(Throwable cause) {
        super(cause);
    }
}

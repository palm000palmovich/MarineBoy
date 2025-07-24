package org.gaming.demo.exceptions;

public class SendingToGamingServiceException extends RuntimeException {
    public SendingToGamingServiceException(String message) {
        super(message);
    }
}

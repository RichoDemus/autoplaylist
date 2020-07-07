package com.richodemus.reader.backend.exception;

public class UserNotSubscribedToThatChannelException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UserNotSubscribedToThatChannelException(String msg) {
        super(msg);
    }
}

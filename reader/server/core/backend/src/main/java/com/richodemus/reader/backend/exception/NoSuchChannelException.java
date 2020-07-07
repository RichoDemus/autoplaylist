package com.richodemus.reader.backend.exception;

public class NoSuchChannelException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public NoSuchChannelException(String s) {
        super(s);
    }
}

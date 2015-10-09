package com.richo.reader.backend.exception;

public class UserNotSubscribedToThatChannelException extends RuntimeException
{
	public UserNotSubscribedToThatChannelException(String msg)
	{
		super(msg);
	}
}

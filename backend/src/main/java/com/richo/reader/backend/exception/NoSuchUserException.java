package com.richo.reader.backend.exception;

public class NoSuchUserException extends RuntimeException
{
	public NoSuchUserException(String msg)
	{
		super(msg);
	}
}

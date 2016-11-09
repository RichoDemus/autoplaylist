package com.richo.reader.backend.exception;

public class NoSuchUserException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public NoSuchUserException(String msg)
	{
		super(msg);
	}
}

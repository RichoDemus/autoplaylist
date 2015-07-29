package com.richo.reader.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NoSuchUserException extends Exception
{
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public NoSuchUserException(String msg)
	{
		super(msg);
	}
}

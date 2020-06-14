package com.richo.reader.backend.exception;

public class NoSuchLabelException extends Exception
{
	private static final long serialVersionUID = 1L;

	public NoSuchLabelException(String msg)
	{
		super(msg);
	}
}

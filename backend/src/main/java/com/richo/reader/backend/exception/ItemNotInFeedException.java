package com.richo.reader.backend.exception;

public class ItemNotInFeedException extends Exception
{
	private static final long serialVersionUID = 1L;

	public ItemNotInFeedException(String msg)
	{
		super(msg);
	}
}

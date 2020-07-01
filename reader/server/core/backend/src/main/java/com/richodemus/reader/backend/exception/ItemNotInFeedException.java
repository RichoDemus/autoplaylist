package com.richodemus.reader.backend.exception;

public class ItemNotInFeedException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public ItemNotInFeedException(String msg)
	{
		super(msg);
	}
}

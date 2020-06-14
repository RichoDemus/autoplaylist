package com.richo.reader.test.util.dropwizard;

import com.richo.reader.web.dropwizard.ReaderApplication;

public class ReaderApplicationWithMocks extends ReaderApplication
{
	public ReaderApplicationWithMocks()
	{
		module = new Mocks();
	}
}

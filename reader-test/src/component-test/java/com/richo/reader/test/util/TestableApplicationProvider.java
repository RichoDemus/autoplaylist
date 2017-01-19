package com.richo.reader.test.util;

import com.richo.reader.test.util.dropwizard.TestableReaderApplication;
import com.richo.reader.test.util.dropwizard.YoutubeMock;

public class TestableApplicationProvider
{
    public TestableApplication readerApplication()
    {
        //return new DropwizardContainer("richodemus/reader");
        return new TestableReaderApplication();
    }

    public TestableApplication readerApplication(int youtubeMockPort)
    {
        //return new DropwizardContainer("richodemus/reader");
        return new TestableReaderApplication(youtubeMockPort);
    }

    public TestableApplication youtubeMock()
    {
        return new YoutubeMock();
    }
}

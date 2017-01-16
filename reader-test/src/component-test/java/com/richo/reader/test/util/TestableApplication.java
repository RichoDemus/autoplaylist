package com.richo.reader.test.util;

public interface TestableApplication
{
    String getIp();
    int getAdminPort();
    int getHttpPort();

    void close();
}

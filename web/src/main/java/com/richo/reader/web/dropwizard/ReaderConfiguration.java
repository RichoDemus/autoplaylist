package com.richo.reader.web.dropwizard;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

public class ReaderConfiguration extends Configuration {
    private String temp;
    private int jmsPort;

    @JsonProperty
    public String getTemp() {
        return temp;
    }

    @JsonProperty
    public void setTemp(String temp) {
        this.temp = temp;
    }

    @JsonProperty
    public void setJmsPort(int jmsPort) {
        this.jmsPort = jmsPort;
    }

    @JsonProperty
    public int getJmsPort() {
        return jmsPort;
    }
}

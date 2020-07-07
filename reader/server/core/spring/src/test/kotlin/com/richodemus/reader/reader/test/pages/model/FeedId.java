package com.richodemus.reader.reader.test.pages.model;

public class FeedId {
    private final String value;

    public FeedId(final String value) {
        this.value = value;
    }

    public String toJson() {
        return "\"" + value + "\"";
    }

    @Override
    public String toString() {
        return value;
    }
}

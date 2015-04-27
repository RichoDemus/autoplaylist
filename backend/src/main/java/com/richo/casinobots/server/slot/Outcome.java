package com.richo.casinobots.server.slot;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Outcome {
    private final int hits;
    private final int payout;

    @JsonCreator
    public Outcome(@JsonProperty("hits") int hits, @JsonProperty("payout") int payout) {
        this.hits = hits;
        this.payout = payout;
    }

    public int getHits() {
        return hits;
    }

    public int getPayout() {
        return payout;
    }
}

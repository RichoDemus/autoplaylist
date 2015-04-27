package com.richo.casinobots.server.slot;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Paytable {
    private final List<Outcome> outcomes;

    @JsonCreator
    public Paytable(final @JsonProperty("outcomes") List<Outcome> outcomes) {
        this.outcomes = outcomes;
    }

    public List<Outcome> getOutcomes() {
        return outcomes;
    }
}

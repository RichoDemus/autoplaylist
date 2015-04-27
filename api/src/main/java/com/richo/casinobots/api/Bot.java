package com.richo.casinobots.api;

public interface Bot {
    String getName();
    String getDescription();
    Action nextRound(ScoreTable currentResult);
}

package com.richo.casinobots.server.slot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;

public class SlotMachine {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Paytable paytable;
    private final Random rand;

    public SlotMachine(Random rand) throws IOException {
        this.rand = rand;
        this.paytable = createPayTable();
        logger.info("There are a total of {} hits for a total of {} payout", getTotalAmountOfHits(), getTotalPays());
    }

    public SlotMachine() throws IOException {
        this(new Random());
    }

    private Paytable createPayTable() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(getClass().getClassLoader().getResourceAsStream("paytable_pacyniak.json"), Paytable.class);
    }

    public long spin(long bet) {
        int steps = rand.nextInt(getTotalAmountOfHits());
        logger.info("Walking {} steps", steps);
        int position = -1;
        while (steps >= 0) {
            position++;
            final int nextStep = paytable.getOutcomes().get(position).getHits();
            logger.info("steps left: {}, current hit size: {}, payout: {}", steps, nextStep, paytable.getOutcomes().get(position).getPayout());
            steps -= nextStep;
        }
        return paytable.getOutcomes().get(position).getPayout();
    }

    private int getTotalAmountOfHits() {
        return paytable.getOutcomes().stream().mapToInt(Outcome::getHits).sum();
    }

    private int getTotalPays() {
        return paytable.getOutcomes().stream().mapToInt(Outcome::getPayout).sum();
    }
}

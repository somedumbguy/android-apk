package com.example.hello.domain.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;

public class StrengthTest {

    @Test
    public void epleyEstimate() {
        assertEquals(215.83, Strength.e1rm(185, 5), 0.01);
        assertEquals(228.0, Strength.e1rm(190, 6), 0.01);
    }

    @Test
    public void aSingleIsItsOwnMaxAndNoRepsEstimatesNothing() {
        assertEquals(315, Strength.e1rm(315, 1), 0);
        assertEquals(0, Strength.e1rm(315, 0), 0);
    }

    @Test
    public void bestAcrossSets() {
        assertEquals(
                228.0,
                Strength.bestE1rm(
                        Arrays.asList(new SetRecord(190, 6, 5), new SetRecord(200, 2, 5), new SetRecord(190, 4, 5))),
                0.01);
    }
}

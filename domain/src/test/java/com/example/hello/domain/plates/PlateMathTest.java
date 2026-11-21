package com.example.hello.domain.plates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class PlateMathTest {
    private final PlateMath plates = PlateMath.standard();

    @Test
    public void loadsHeaviestPlatesFirst() {
        assertEquals(Arrays.asList(45.0, 25.0, 2.5), plates.perSide(190));
        assertEquals(Arrays.asList(45.0, 25.0, 2.5, 1.25), plates.perSide(192.5));
        assertEquals(Arrays.asList(45.0, 45.0), plates.perSide(225));
    }

    @Test
    public void describesSpecialCases() {
        assertEquals("Empty bar", plates.describePerSide(45));
        assertEquals("Lighter than the bar", plates.describePerSide(40));
        assertEquals("45 · 25 · 2.5", plates.describePerSide(190));
    }

    @Test
    public void warmupsRampUpToTheWorkWeight() {
        List<PlateMath.Warmup> w = plates.warmups(190);
        assertEquals(4, w.size());
        assertEquals(45, w.get(0).weight, 0);
        assertEquals(10, w.get(0).reps);
        assertEquals(95, w.get(1).weight, 0);
        assertEquals(135, w.get(2).weight, 0);
        assertEquals(160, w.get(3).weight, 0);
        for (PlateMath.Warmup x : w) assertTrue(x.weight < 190);
    }

    @Test
    public void lightWorkWeightsNeedNoWarmups() {
        assertTrue(plates.warmups(65).isEmpty());
    }

    @Test
    public void supportsOtherBarsAndPlates() {
        PlateMath kg = new PlateMath(20, new double[] {25, 20, 10, 5, 2.5, 1.25});
        assertEquals(Arrays.asList(25.0, 5.0), kg.perSide(80));
    }
}

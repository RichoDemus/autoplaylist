package com.richo.casinobots.server.slot;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.stubbing.OngoingStubbing;

import java.util.Random;

public class SlotMachineTest {

    private Random random;

    @Before
    public void setUp() throws Exception {
        random = Mockito.mock(Random.class);
    }

    @Test
    public void testSpinZeroSteps() throws Exception {
        Mockito.when(random.nextInt(Matchers.anyInt())).thenReturn(0);
        final SlotMachine machine = new SlotMachine(random);
        final long result = machine.spin(1L);
        Assert.assertEquals(2500, result);
    }

    @Test
    public void testSpinFourSteps() throws Exception {
        Mockito.when(random.nextInt(Matchers.anyInt())).thenReturn(4);
        final SlotMachine machine = new SlotMachine(random);
        final long result = machine.spin(1L);
        Assert.assertEquals(1800, result);
    }
    @Test
    public void testSpinFiveSteps() throws Exception {
        Mockito.when(random.nextInt(Matchers.anyInt())).thenReturn(5);
        final SlotMachine machine = new SlotMachine(random);
        final long result = machine.spin(1L);
        Assert.assertEquals(720, result);
    }

    @Test
    public void testSpin4131Steps() throws Exception {
        Mockito.when(random.nextInt(Matchers.anyInt())).thenReturn(4131);
        final SlotMachine machine = new SlotMachine(random);
        final long result = machine.spin(1L);
        Assert.assertEquals(10, result);
    }
}
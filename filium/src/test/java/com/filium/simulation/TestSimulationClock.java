package com.filium.simulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestSimulationClock {

    private SimulationClock clock;

    @BeforeEach void setUp() { clock = new SimulationClock(); }

    @Test void constructor_notRunning() { assertFalse(clock.isRunning()); }
    @Test void constructor_tickCountZero() { assertEquals(0, clock.getTickCount()); }
    @Test void constructor_defaultSpeed() {
        assertEquals(SimulationClock.DEFAULT_SPEED, clock.getSpeed());
    }

    // start / stop
    @Test void start_setsRunningTrue() { clock.start(); assertTrue(clock.isRunning()); }
    @Test void start_alreadyRunning_noEffect() {
        clock.start(); clock.start(); assertTrue(clock.isRunning());
    }
    @Test void stop_setsRunningFalse() { clock.start(); clock.stop(); assertFalse(clock.isRunning()); }
    @Test void stop_alreadyStopped_noEffect() { clock.stop(); assertFalse(clock.isRunning()); }

    // tick
    @Test void tick_whenRunning_incrementsCount() {
        clock.start(); clock.tick(); assertEquals(1, clock.getTickCount());
    }
    @Test void tick_whenStopped_doesNotIncrement() {
        clock.tick(); assertEquals(0, clock.getTickCount());
    }
    @Test void tick_multipleTimesWhileRunning_accumulatesCount() {
        clock.start();
        for (int i = 0; i < 5; i++) clock.tick();
        assertEquals(5, clock.getTickCount());
    }

    // setSpeed
    @Test void setSpeed_atMin_doesNotThrow() {
        assertDoesNotThrow(() -> clock.setSpeed(SimulationClock.MIN_SPEED));
    }
    @Test void setSpeed_atMax_doesNotThrow() {
        assertDoesNotThrow(() -> clock.setSpeed(SimulationClock.MAX_SPEED));
    }
    @Test void setSpeed_belowMin_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> clock.setSpeed(SimulationClock.MIN_SPEED - 0.01));
    }
    @Test void setSpeed_aboveMax_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> clock.setSpeed(SimulationClock.MAX_SPEED + 0.01));
    }
    @Test void setSpeed_validValue_updatesSpeed() {
        clock.setSpeed(2.0); assertEquals(2.0, clock.getSpeed());
    }

    // reset
    @Test void reset_stopsClockAndZerosTicks() {
        clock.start(); clock.tick(); clock.tick(); clock.reset();
        assertEquals(0, clock.getTickCount());
        assertFalse(clock.isRunning());
    }
}
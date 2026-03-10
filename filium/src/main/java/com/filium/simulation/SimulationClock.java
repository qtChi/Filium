package com.filium.simulation;

/**
 * Controls simulation tick speed, independent of wall-clock time.
 * The SimulationEngine advances one tick per call to {@link #tick()}.
 * Speed is expressed as a multiplier: 1.0 = normal, 2.0 = double speed.
 */
public class SimulationClock {

    public static final double MIN_SPEED = 0.25;
    public static final double MAX_SPEED = 8.0;
    public static final double DEFAULT_SPEED = 1.0;

    private long tickCount;
    private double speed;
    private boolean running;

    /** Constructs a SimulationClock at default speed, not running. */
    public SimulationClock() {
        this.tickCount = 0;
        this.speed     = DEFAULT_SPEED;
        this.running   = false;
    }

    /**
     * Starts the simulation clock.
     * Has no effect if already running.
     */
    public void start() {
        running = true;
    }

    /**
     * Pauses the simulation clock.
     * Has no effect if already stopped.
     */
    public void stop() {
        running = false;
    }

    /**
     * Returns true if the clock is currently running.
     *
     * @return true if running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Advances the tick counter by one if the clock is running.
     * Does nothing if paused.
     */
    public void tick() {
        if (running) {
            tickCount++;
        }
    }

    /**
     * Returns the total number of ticks elapsed since the clock was created or reset.
     *
     * @return tick count
     */
    public long getTickCount() {
        return tickCount;
    }

    /**
     * Returns the current simulation speed multiplier.
     *
     * @return speed between MIN_SPEED and MAX_SPEED inclusive
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Sets the simulation speed multiplier.
     *
     * @param speed the new speed; must be between MIN_SPEED and MAX_SPEED inclusive
     * @throws IllegalArgumentException if speed is out of range
     */
    public void setSpeed(double speed) {
        if (speed < MIN_SPEED || speed > MAX_SPEED) {
            throw new IllegalArgumentException(
                "Speed must be between " + MIN_SPEED + " and " + MAX_SPEED
                + ", got: " + speed);
        }
        this.speed = speed;
    }

    /**
     * Resets the tick counter to zero and stops the clock.
     */
    public void reset() {
        tickCount = 0;
        running   = false;
    }
}
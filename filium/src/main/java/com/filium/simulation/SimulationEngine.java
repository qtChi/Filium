package com.filium.simulation;

import com.filium.model.network.NetworkTopology;
import com.filium.packet.Packet;
import com.filium.simulation.protocols.Protocol;
import com.filium.simulation.protocols.ProtocolRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Central coordinator of the Filium simulation.
 * Drives the packet queue, dispatches packets to protocol handlers,
 * and emits simulation events to registered listeners.
 */
public class SimulationEngine {

    private final NetworkTopology topology;
    private final PacketQueue queue;
    private final ProtocolRegistry registry;
    private final SimulationClock clock;
    private final List<SimulationEvent> eventLog;
    private final List<Consumer<SimulationEvent>> listeners;

    /**
     * Constructs a SimulationEngine with all dependencies injected.
     *
     * @param topology the network topology to simulate; must not be null
     * @param registry the protocol handler registry; must not be null
     * @param clock    the simulation clock; must not be null
     * @throws IllegalArgumentException if any argument is null
     */
    public SimulationEngine(NetworkTopology topology,
                             ProtocolRegistry registry,
                             SimulationClock clock) {
        if (topology == null) throw new IllegalArgumentException("Topology must not be null");
        if (registry == null) throw new IllegalArgumentException("Registry must not be null");
        if (clock    == null) throw new IllegalArgumentException("Clock must not be null");
        this.topology  = topology;
        this.registry  = registry;
        this.clock     = clock;
        this.queue     = new PacketQueue();
        this.eventLog  = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    /**
     * Enqueues a packet for processing on the next tick.
     *
     * @param packet the packet to inject; must not be null
     * @throws IllegalArgumentException if packet is null
     */
    public void sendPacket(Packet packet) {
        if (packet == null) throw new IllegalArgumentException("Packet must not be null");
        queue.enqueue(packet);
    }

    /**
     * Advances the simulation by one tick.
     * Drains the entire current queue, dispatching each packet to the
     * appropriate protocol handler and firing all resulting events.
     * Does nothing if the clock is not running.
     */
    public void tick() {
        if (!clock.isRunning()) return;
        clock.tick();

        List<Packet> batch = new ArrayList<>();
        while (!queue.isEmpty()) {
            queue.dequeue().ifPresent(batch::add);
        }

        for (Packet packet : batch) {
            Optional<Protocol> handler = registry.findHandler(packet);
            if (handler.isPresent()) {
                List<SimulationEvent> events =
                    handler.get().handle(packet, topology, queue);
                for (SimulationEvent event : events) {
                    emit(event);
                }
            } else {
                SimulationEvent dropped = new SimulationEvent(
                    SimulationEventType.PACKET_DROPPED,
                    null, null, packet,
                    "No handler for packet type: " + packet.getType());
                emit(dropped);
            }
        }
    }

    /**
     * Registers a listener that will be called for every SimulationEvent emitted.
     *
     * @param listener the event consumer; must not be null
     * @throws IllegalArgumentException if listener is null
     */
    public void addListener(Consumer<SimulationEvent> listener) {
        if (listener == null) throw new IllegalArgumentException("Listener must not be null");
        listeners.add(listener);
    }

    /**
     * Removes a previously registered listener.
     *
     * @param listener the listener to remove
     * @return true if it was registered and has been removed
     */
    public boolean removeListener(Consumer<SimulationEvent> listener) {
        return listeners.remove(listener);
    }

    /**
     * Returns an unmodifiable view of the full event log.
     *
     * @return unmodifiable list of SimulationEvent
     */
    public List<SimulationEvent> getEventLog() {
        return Collections.unmodifiableList(eventLog);
    }

    /**
     * Returns the current packet queue size.
     *
     * @return number of packets waiting
     */
    public int getQueueSize() {
        return queue.size();
    }

    /**
     * Resets the engine: clears the queue, event log, and resets the clock.
     */
    public void reset() {
        queue.clear();
        eventLog.clear();
        clock.reset();
    }

    private void emit(SimulationEvent event) {
        eventLog.add(event);
        for (Consumer<SimulationEvent> listener : listeners) {
            listener.accept(event);
        }
    }
}
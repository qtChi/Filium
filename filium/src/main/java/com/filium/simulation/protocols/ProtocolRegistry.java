package com.filium.simulation.protocols;

import com.filium.packet.Packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Maintains the ordered list of registered Protocol handlers.
 * The SimulationEngine queries this registry to find the correct
 * handler for each incoming packet.
 */
public class ProtocolRegistry {

    private final List<Protocol> handlers;

    /** Constructs an empty ProtocolRegistry. */
    public ProtocolRegistry() {
        this.handlers = new ArrayList<>();
    }

    /**
     * Registers a protocol handler.
     *
     * @param protocol the handler to register; must not be null
     * @throws IllegalArgumentException if protocol is null
     */
    public void register(Protocol protocol) {
        if (protocol == null) {
            throw new IllegalArgumentException("Protocol must not be null");
        }
        handlers.add(protocol);
    }

    /**
     * Removes a protocol handler.
     *
     * @param protocol the handler to remove
     * @return true if the handler was registered and has been removed
     */
    public boolean unregister(Protocol protocol) {
        return handlers.remove(protocol);
    }

    /**
     * Returns the first registered handler that can process the given packet.
     *
     * @param packet the packet to find a handler for; must not be null
     * @return Optional containing the matching Protocol, or empty if none found
     * @throws IllegalArgumentException if packet is null
     */
    public Optional<Protocol> findHandler(Packet packet) {
        if (packet == null) {
            throw new IllegalArgumentException("Packet must not be null");
        }
        return handlers.stream()
            .filter(h -> h.canHandle(packet))
            .findFirst();
    }

    /**
     * Returns an unmodifiable view of all registered handlers.
     *
     * @return unmodifiable list of Protocol
     */
    public List<Protocol> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }

    /**
     * Returns the number of registered handlers.
     *
     * @return handler count
     */
    public int size() {
        return handlers.size();
    }
}
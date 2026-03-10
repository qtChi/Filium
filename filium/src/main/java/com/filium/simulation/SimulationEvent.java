package com.filium.simulation;

import com.filium.model.devices.Device;
import com.filium.packet.Packet;

import java.util.Objects;

/**
 * Immutable record of something that happened during simulation.
 * Emitted by SimulationEngine and consumed by the UI log panel and
 * packet animation layer.
 */
public final class SimulationEvent {

    private final SimulationEventType type;
    private final Device source;
    private final Device destination;
    private final Packet packet;
    private final String message;
    private final long timestamp;

    /**
     * Constructs a SimulationEvent.
     *
     * @param type        the kind of event; must not be null
     * @param source      the originating device; may be null for system events
     * @param destination the target device; may be null
     * @param packet      the packet involved; may be null for non-packet events
     * @param message     human-readable description; must not be null
     * @throws IllegalArgumentException if type or message is null
     */
    public SimulationEvent(SimulationEventType type,
                           Device source,
                           Device destination,
                           Packet packet,
                           String message) {
        if (type == null) {
            throw new IllegalArgumentException("SimulationEventType must not be null");
        }
        if (message == null) {
            throw new IllegalArgumentException("Message must not be null");
        }
        this.type        = type;
        this.source      = source;
        this.destination = destination;
        this.packet      = packet;
        this.message     = message;
        this.timestamp   = System.currentTimeMillis();
    }

    public SimulationEventType getType()  { return type; }
    public Device getSource()             { return source; }
    public Device getDestination()        { return destination; }
    public Packet getPacket()             { return packet; }
    public String getMessage()            { return message; }
    public long getTimestamp()            { return timestamp; }

    @Override
    public String toString() {
        return "SimulationEvent{type=" + type
            + ", src=" + (source != null ? source.getName() : "null")
            + ", dst=" + (destination != null ? destination.getName() : "null")
            + ", msg=" + message + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimulationEvent other)) return false;
        return timestamp == other.timestamp
            && type == other.type
            && Objects.equals(source, other.source)
            && Objects.equals(destination, other.destination)
            && Objects.equals(packet, other.packet)
            && message.equals(other.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, source, destination, packet, message, timestamp);
    }
}
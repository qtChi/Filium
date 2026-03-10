package com.filium.packet;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a network packet travelling through the Filium simulation.
 * Immutable once created. Use {@link #withHeader(PacketHeader)} to produce
 * a new Packet with an updated header (e.g. after TTL decrement at a router).
 */
public final class Packet {

    private final String id;
    private final PacketType type;
    private final PacketHeader header;
    private final String payload;
    private final long createdAt;

    /**
     * Constructs a new Packet. A UUID is auto-generated as the packet identifier.
     *
     * @param type    the protocol type of this packet; must not be null
     * @param header  the IP/MAC header; must not be null
     * @param payload the string payload (e.g. DNS hostname, HTTP request line);
     *                null is treated as an empty string
     * @throws IllegalArgumentException if type or header is null
     */
    public Packet(PacketType type, PacketHeader header, String payload) {
        if (type == null) {
            throw new IllegalArgumentException("PacketType must not be null");
        }
        if (header == null) {
            throw new IllegalArgumentException("PacketHeader must not be null");
        }
        this.id = UUID.randomUUID().toString();
        this.type = type;
        this.header = header;
        this.payload = payload != null ? payload : "";
        this.createdAt = System.currentTimeMillis();
    }

    /**
     * Private constructor used by {@link #withHeader(PacketHeader)} to preserve
     * the original packet ID and creation timestamp.
     */
    private Packet(String id, PacketType type, PacketHeader header,
                   String payload, long createdAt) {
        this.id = id;
        this.type = type;
        this.header = header;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    /**
     * Returns a new Packet that is identical to this one but with the given header.
     * The packet ID and creation timestamp are preserved.
     * Used by routers to decrement TTL without creating an entirely new packet.
     *
     * @param newHeader the replacement header; must not be null
     * @return new Packet with the updated header
     * @throws IllegalArgumentException if newHeader is null
     */
    public Packet withHeader(PacketHeader newHeader) {
        if (newHeader == null) {
            throw new IllegalArgumentException("New header must not be null");
        }
        return new Packet(id, type, newHeader, payload, createdAt);
    }

    /**
     * Returns the unique identifier for this packet.
     *
     * @return UUID string
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the protocol type of this packet.
     *
     * @return PacketType
     */
    public PacketType getType() {
        return type;
    }

    /**
     * Returns the IP/MAC header of this packet.
     *
     * @return PacketHeader
     */
    public PacketHeader getHeader() {
        return header;
    }

    /**
     * Returns the string payload of this packet.
     * Never null — defaults to empty string if null was passed at construction.
     *
     * @return payload string
     */
    public String getPayload() {
        return payload;
    }

    /**
     * Returns the creation timestamp in milliseconds since epoch.
     *
     * @return creation time as long
     */
    public long getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Packet other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Packet{id=" + id + ", type=" + type +
               ", src=" + header.getSourceIP() +
               ", dst=" + header.getDestinationIP() +
               ", ttl=" + header.getTtl() + "}";
    }
}
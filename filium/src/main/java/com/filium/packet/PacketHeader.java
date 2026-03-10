package com.filium.packet;

import com.filium.model.network.IPAddress;
import com.filium.model.network.NetworkInterface;
import java.util.Objects;

/**
 * Immutable value class representing the header fields shared by all packets.
 * Holds source/destination IP and MAC addresses and a TTL counter.
 * Every mutating operation returns a new instance — the original is never modified.
 */
public class PacketHeader {

    private final IPAddress sourceIP;
    private final IPAddress destinationIP;
    private final String sourceMAC;
    private final String destinationMAC;
    private final int ttl;

    /**
     * Constructs a PacketHeader with all required fields.
     *
     * @param sourceIP       the sender's IP address
     * @param destinationIP  the target IP address
     * @param sourceMAC      the sender's MAC address in XX:XX:XX:XX:XX:XX format
     * @param destinationMAC the target MAC address in XX:XX:XX:XX:XX:XX format
     * @param ttl            time-to-live value; must be between 1 and 255 inclusive
     * @throws IllegalArgumentException if any argument is null, MAC format is invalid,
     *                                  or TTL is out of range
     */
    public PacketHeader(IPAddress sourceIP,
                        IPAddress destinationIP,
                        String sourceMAC,
                        String destinationMAC,
                        int ttl) {
        if (sourceIP == null) {
            throw new IllegalArgumentException("Source IP must not be null");
        }
        if (destinationIP == null) {
            throw new IllegalArgumentException("Destination IP must not be null");
        }
        if (sourceMAC == null || !NetworkInterface.isValidMAC(sourceMAC)) {
            throw new IllegalArgumentException(
                "Invalid source MAC address: '" + sourceMAC + "'");
        }
        if (destinationMAC == null || !NetworkInterface.isValidMAC(destinationMAC)) {
            throw new IllegalArgumentException(
                "Invalid destination MAC address: '" + destinationMAC + "'");
        }
        if (ttl < 1 || ttl > 255) {
            throw new IllegalArgumentException(
                "TTL must be between 1 and 255, got: " + ttl);
        }
        this.sourceIP = sourceIP;
        this.destinationIP = destinationIP;
        this.sourceMAC = sourceMAC;
        this.destinationMAC = destinationMAC;
        this.ttl = ttl;
    }

    /**
     * Returns the source IP address.
     *
     * @return source IPAddress
     */
    public IPAddress getSourceIP() {
        return sourceIP;
    }

    /**
     * Returns the destination IP address.
     *
     * @return destination IPAddress
     */
    public IPAddress getDestinationIP() {
        return destinationIP;
    }

    /**
     * Returns the source MAC address.
     *
     * @return source MAC string
     */
    public String getSourceMAC() {
        return sourceMAC;
    }

    /**
     * Returns the destination MAC address.
     *
     * @return destination MAC string
     */
    public String getDestinationMAC() {
        return destinationMAC;
    }

    /**
     * Returns the current TTL value.
     *
     * @return TTL integer between 1 and 255
     */
    public int getTtl() {
        return ttl;
    }

    /**
     * Returns a new PacketHeader with TTL decremented by one.
     * The TTL may reach 0 on the returned header — check {@link #isExpired()}.
     *
     * @return new PacketHeader with ttl - 1
     * @throws IllegalStateException if TTL is already 0
     */
    public PacketHeader decrementTTL() {
        if (ttl <= 0) {
            throw new IllegalStateException("Cannot decrement TTL that is already 0");
        }
        if (ttl == 1) {
            return new PacketHeaderExpired(sourceIP, destinationIP,
                sourceMAC, destinationMAC);
        }
        return new PacketHeader(sourceIP, destinationIP,
            sourceMAC, destinationMAC, ttl - 1);
    }

    /**
     * Returns true if this header's TTL has reached zero, meaning the packet
     * should be dropped and an ICMP TTL Exceeded message should be sent.
     *
     * @return true if TTL is 0
     */
    public boolean isExpired() {
        return ttl == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PacketHeader other)) return false;
        return ttl == other.ttl
            && sourceIP.equals(other.sourceIP)
            && destinationIP.equals(other.destinationIP)
            && sourceMAC.equals(other.sourceMAC)
            && destinationMAC.equals(other.destinationMAC);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceIP, destinationIP, sourceMAC, destinationMAC, ttl);
    }

    @Override
    public String toString() {
        return "PacketHeader{src=" + sourceIP + " (" + sourceMAC + ")" +
               ", dst=" + destinationIP + " (" + destinationMAC + ")" +
               ", ttl=" + ttl + "}";
    }

    /**
     * Internal subclass representing a header whose TTL has reached zero.
     * Overrides isExpired() to return true without storing TTL=0 in the
     * public constructor (which validates TTL >= 1).
     */
    private static final class PacketHeaderExpired extends PacketHeader {

        private PacketHeaderExpired(IPAddress sourceIP, IPAddress destinationIP,
                                    String sourceMAC, String destinationMAC) {
            // Use TTL=1 to satisfy parent constructor validation,
            // then override getTtl() and isExpired() below.
            super(sourceIP, destinationIP, sourceMAC, destinationMAC, 1);
        }

        @Override
        public int getTtl() {
            return 0;
        }

        @Override
        public boolean isExpired() {
            return true;
        }

        @Override
        public PacketHeader decrementTTL() {
            throw new IllegalStateException("Cannot decrement TTL that is already 0");
        }
    }
}
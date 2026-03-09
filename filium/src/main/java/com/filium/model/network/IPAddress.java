package com.filium.model.network;

import java.util.Objects;

/**
 * Immutable value class representing an IPv4 address.
 * Validates format on construction. All instances are guaranteed
 * to hold a well-formed dotted-decimal IPv4 address.
 */
public final class IPAddress {

    /** Broadcast address constant: 255.255.255.255 */
    public static final IPAddress BROADCAST = new IPAddress("255.255.255.255");

    /** Loopback address constant: 127.0.0.1 */
    public static final IPAddress LOOPBACK = new IPAddress("127.0.0.1");

    private final String address;
    private final int[] octets;

    /**
     * Constructs a new IPAddress from a dotted-decimal string.
     *
     * @param address the dotted-decimal IPv4 string (e.g. "192.168.1.1")
     * @throws IllegalArgumentException if the format is invalid
     */
    public IPAddress(String address) {
        if (address == null) {
            throw new IllegalArgumentException("IP address must not be null");
        }
        this.octets = parse(address.trim());
        this.address = address.trim();
    }

    /**
     * Parses a dotted-decimal string into 4 octet integers.
     * Throws IllegalArgumentException on any format violation.
     */
    private static int[] parse(String address) {
        String[] parts = address.split("\\.", -1);

        if (parts.length != 4) {
            throw new IllegalArgumentException(
                "Invalid IP address format: '" + address + "'. Expected 4 octets.");
        }

        int[] octets = new int[4];
        for (int i = 0; i < 4; i++) {
            String part = parts[i];
            if (part.isEmpty()) {
                throw new IllegalArgumentException(
                    "Invalid IP address format: '" + address + "'. Empty octet at position " + i + ".");
            }
            try {
                octets[i] = Integer.parseInt(part);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Invalid IP address format: '" + address + "'. Non-numeric octet '" + part + "'.");
            }
            if (octets[i] < 0 || octets[i] > 255) {
                throw new IllegalArgumentException(
                    "Invalid IP address format: '" + address + "'. Octet value " + octets[i] + " out of range 0-255.");
            }
        }
        return octets;
    }

    /**
     * Returns the IP address as a dotted-decimal string.
     *
     * @return dotted-decimal string, e.g. "192.168.1.1"
     */
    public String getAddress() {
        return address;
    }

    /**
     * Returns a copy of the 4 octet integer values.
     *
     * @return int array of length 4, each value 0-255
     */
    public int[] getOctets() {
        return octets.clone();
    }

    /**
     * Determines whether this IP address falls within the given network/mask.
     *
     * @param network the network address (e.g. "192.168.1.0")
     * @param mask    the subnet mask in dotted-decimal (e.g. "255.255.255.0")
     * @return true if this address is within the specified subnet
     * @throws IllegalArgumentException if network or mask is invalid
     */
    public boolean isInSubnet(IPAddress network, String mask) {
        if (network == null) {
            throw new IllegalArgumentException("Network address must not be null");
        }
        if (mask == null) {
            throw new IllegalArgumentException("Subnet mask must not be null");
        }

        int[] maskOctets = parse(mask);
        int[] networkOctets = network.getOctets();
        int[] thisOctets = this.octets;

        for (int i = 0; i < 4; i++) {
            if ((thisOctets[i] & maskOctets[i]) != (networkOctets[i] & maskOctets[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates an IPAddress from four integer octet values.
     *
     * @param a first octet
     * @param b second octet
     * @param c third octet
     * @param d fourth octet
     * @return new IPAddress
     * @throws IllegalArgumentException if any octet is out of range
     */
    public static IPAddress of(int a, int b, int c, int d) {
        return new IPAddress(a + "." + b + "." + c + "." + d);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IPAddress other)) return false;
        return address.equals(other.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }

    @Override
    public String toString() {
        return address;
    }
}
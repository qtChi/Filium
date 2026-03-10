package com.filium.model.network;

import java.util.Objects;
import java.util.Random;

/**
 * Represents a network interface card (NIC) on a device.
 * Holds IP address, subnet mask, default gateway, and MAC address.
 * A device may be unconfigured (no IP assigned) or fully configured.
 */
public class NetworkInterface {

    private static final String DEFAULT_SUBNET_MASK = "255.255.255.0";
    private static final Random RANDOM = new Random();

    private final String macAddress;
    private IPAddress ipAddress;
    private String subnetMask;
    private IPAddress defaultGateway;

    /**
     * Constructs a new NetworkInterface with an auto-generated MAC address.
     * IP address and gateway are null until explicitly configured.
     */
    public NetworkInterface() {
        this.macAddress = generateMAC();
        this.subnetMask = DEFAULT_SUBNET_MASK;
        this.ipAddress = null;
        this.defaultGateway = null;
    }

    /**
     * Constructs a NetworkInterface with a specific MAC address.
     * Useful for testing and deserialization.
     *
     * @param macAddress the MAC address string in XX:XX:XX:XX:XX:XX format
     * @throws IllegalArgumentException if the MAC address format is invalid
     */
    public NetworkInterface(String macAddress) {
        if (!isValidMAC(macAddress)) {
            throw new IllegalArgumentException(
                "Invalid MAC address format: '" + macAddress + "'. Expected XX:XX:XX:XX:XX:XX.");
        }
        this.macAddress = macAddress;
        this.subnetMask = DEFAULT_SUBNET_MASK;
        this.ipAddress = null;
        this.defaultGateway = null;
    }

    /**
     * Returns the MAC address of this interface.
     *
     * @return MAC address string in XX:XX:XX:XX:XX:XX format
     */
    public String getMacAddress() {
        return macAddress;
    }

    /**
     * Returns the assigned IP address, or null if not configured.
     *
     * @return IPAddress or null
     */
    public IPAddress getIpAddress() {
        return ipAddress;
    }

    /**
     * Sets the IP address on this interface.
     *
     * @param ipAddress the IP address to assign; null clears the configuration
     */
    public void setIpAddress(IPAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Returns the subnet mask.
     *
     * @return subnet mask in dotted-decimal format; default is "255.255.255.0"
     */
    public String getSubnetMask() {
        return subnetMask;
    }

    /**
     * Sets the subnet mask.
     *
     * @param subnetMask dotted-decimal subnet mask string
     * @throws IllegalArgumentException if subnetMask is null or blank
     */
    public void setSubnetMask(String subnetMask) {
        if (subnetMask == null || subnetMask.isBlank()) {
            throw new IllegalArgumentException("Subnet mask must not be null or blank");
        }
        this.subnetMask = subnetMask;
    }

    /**
     * Returns the default gateway IP address, or null if not configured.
     *
     * @return IPAddress of the gateway, or null
     */
    public IPAddress getDefaultGateway() {
        return defaultGateway;
    }

    /**
     * Sets the default gateway IP address.
     *
     * @param defaultGateway the gateway IP; null clears the gateway
     */
    public void setDefaultGateway(IPAddress defaultGateway) {
        this.defaultGateway = defaultGateway;
    }

    /**
     * Returns true if this interface has an IP address assigned.
     *
     * @return true if ipAddress is not null
     */
    public boolean isConfigured() {
        return ipAddress != null;
    }

    /**
     * Generates a random valid MAC address string in XX:XX:XX:XX:XX:XX format.
     * The first octet has the locally administered bit set and multicast bit cleared,
     * which is conventional for generated/virtual MAC addresses.
     *
     * @return a randomly generated MAC address string
     */
    public static String generateMAC() {
        byte[] bytes = new byte[6];
        RANDOM.nextBytes(bytes);
        // Set locally administered bit, clear multicast bit on first octet
        bytes[0] = (byte) ((bytes[0] & 0xFE) | 0x02);
        return String.format("%02X:%02X:%02X:%02X:%02X:%02X",
            bytes[0], bytes[1], bytes[2], bytes[3], bytes[4], bytes[5]);
    }

    /**
     * Validates that a MAC address string matches the XX:XX:XX:XX:XX:XX format,
     * where each XX is a hexadecimal byte (case-insensitive).
     *
     * @param mac the MAC address string to validate
     * @return true if the format is valid
     */
    public static boolean isValidMAC(String mac) {
        if (mac == null) return false;
        return mac.matches("^([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}$");
    }

    @Override
    public String toString() {
        return "NetworkInterface{mac=" + macAddress +
               ", ip=" + (ipAddress != null ? ipAddress : "unset") +
               ", mask=" + subnetMask +
               ", gw=" + (defaultGateway != null ? defaultGateway : "unset") + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NetworkInterface other)) return false;
        return macAddress.equals(other.macAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(macAddress);
    }
}
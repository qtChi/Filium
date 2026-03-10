package com.filium.model.devices;

import com.filium.model.network.IPAddress;
import com.filium.packet.Packet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a DHCP server. Extends Device.
 * Maintains a pool of available IP addresses and a lease table
 * mapping device MAC addresses to assigned IPs.
 * DHCP handshake logic is delegated to DHCPHandler in the simulation layer.
 */
public class DHCPServer extends Device {

    private IPAddress poolStart;
    private IPAddress poolEnd;
    private final Map<String, IPAddress> leases;

    /**
     * Constructs a new DHCPServer with the given name.
     *
     * @param name the user-assigned label; must not be null
     */
    public DHCPServer(String name) {
        super(name, DeviceType.DHCP_SERVER);
        this.leases = new HashMap<>();
        this.poolStart = null;
        this.poolEnd = null;
    }

    /**
     * Configures the IP address pool range.
     *
     * @param start the first IP in the pool; must not be null
     * @param end   the last IP in the pool; must not be null
     * @throws IllegalArgumentException if start or end is null
     */
    public void setPool(IPAddress start, IPAddress end) {
        if (start == null) {
            throw new IllegalArgumentException("Pool start address must not be null");
        }
        if (end == null) {
            throw new IllegalArgumentException("Pool end address must not be null");
        }
        this.poolStart = start;
        this.poolEnd = end;
    }

    /**
     * Returns the start of the IP address pool.
     *
     * @return pool start IPAddress, or null if not configured
     */
    public IPAddress getPoolStart() {
        return poolStart;
    }

    /**
     * Returns the end of the IP address pool.
     *
     * @return pool end IPAddress, or null if not configured
     */
    public IPAddress getPoolEnd() {
        return poolEnd;
    }

    /**
     * Assigns the next available IP address from the pool to a device MAC address.
     * Iterates through the pool range sequentially, skipping already-leased IPs.
     *
     * @param mac the MAC address of the requesting device; must not be null or blank
     * @return Optional containing the assigned IPAddress, or empty if pool is exhausted
     *         or not configured
     * @throws IllegalArgumentException if mac is null or blank
     */
    public Optional<IPAddress> assignIP(String mac) {
        if (mac == null || mac.isBlank()) {
            throw new IllegalArgumentException("MAC address must not be null or blank");
        }
        if (poolStart == null || poolEnd == null) {
            return Optional.empty();
        }

        int[] start = poolStart.getOctets();
        int[] end = poolEnd.getOctets();

        for (int a = start[0]; a <= end[0]; a++) {
            int bStart = (a == start[0]) ? start[1] : 0;
            int bEnd   = (a == end[0])   ? end[1]   : 255;
            for (int b = bStart; b <= bEnd; b++) {
                int cStart = (a == start[0] && b == start[1]) ? start[2] : 0;
                int cEnd   = (a == end[0]   && b == end[1])   ? end[2]   : 255;
                for (int c = cStart; c <= cEnd; c++) {
                    int dStart = (a == start[0] && b == start[1] && c == start[2]) ? start[3] : 0;
                    int dEnd   = (a == end[0]   && b == end[1]   && c == end[2])   ? end[3]   : 255;
                    for (int d = dStart; d <= dEnd; d++) {
                        IPAddress candidate = IPAddress.of(a, b, c, d);
                        if (!leases.containsValue(candidate)) {
                            leases.put(mac, candidate);
                            return Optional.of(candidate);
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Releases the lease for a given MAC address.
     *
     * @param mac the MAC address whose lease should be released
     * @return true if the lease existed and was released, false if not found
     */
    public boolean releaseIP(String mac) {
        return leases.remove(mac) != null;
    }

    /**
     * Returns an unmodifiable view of all current leases.
     *
     * @return unmodifiable map of MAC address to assigned IPAddress
     */
    public Map<String, IPAddress> getLeases() {
        return Collections.unmodifiableMap(leases);
    }

    /**
     * Receives a packet. DHCP handshake logic is delegated to DHCPHandler.
     *
     * @param packet the incoming packet; must not be null
     * @throws IllegalArgumentException if packet is null
     */
    @Override
    public void receivePacket(Packet packet) {
        if (packet == null) {
            throw new IllegalArgumentException("Packet must not be null");
        }
        // Handshake logic is delegated to DHCPHandler in the simulation layer
    }

    /**
     * Resets this DHCP server by clearing all leases and the pool configuration.
     */
    @Override
    public void reset() {
        leases.clear();
        poolStart = null;
        poolEnd = null;
    }
}
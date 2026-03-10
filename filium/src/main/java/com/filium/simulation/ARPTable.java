package com.filium.simulation;

import com.filium.model.network.IPAddress;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Maintains a mapping of IP address to MAC address for devices on the same
 * Layer 2 segment. Populated by ARPHandler during simulation.
 */
public class ARPTable {

    private final Map<String, String> table; // IP string -> MAC string

    /** Constructs an empty ARPTable. */
    public ARPTable() {
        this.table = new HashMap<>();
    }

    /**
     * Adds or updates an ARP entry.
     *
     * @param ip  the IP address; must not be null
     * @param mac the MAC address string; must not be null or blank
     * @throws IllegalArgumentException if ip is null or mac is null/blank
     */
    public void put(IPAddress ip, String mac) {
        if (ip == null) {
            throw new IllegalArgumentException("IP address must not be null");
        }
        if (mac == null || mac.isBlank()) {
            throw new IllegalArgumentException("MAC address must not be null or blank");
        }
        table.put(ip.getAddress(), mac);
    }

    /**
     * Looks up the MAC address for a given IP.
     *
     * @param ip the IP address to look up; must not be null
     * @return Optional containing the MAC string if found, empty otherwise
     * @throws IllegalArgumentException if ip is null
     */
    public Optional<String> lookup(IPAddress ip) {
        if (ip == null) {
            throw new IllegalArgumentException("IP address must not be null");
        }
        return Optional.ofNullable(table.get(ip.getAddress()));
    }

    /**
     * Removes the ARP entry for the given IP address.
     *
     * @param ip the IP address to remove; must not be null
     * @return true if an entry was removed, false if not found
     * @throws IllegalArgumentException if ip is null
     */
    public boolean remove(IPAddress ip) {
        if (ip == null) {
            throw new IllegalArgumentException("IP address must not be null");
        }
        return table.remove(ip.getAddress()) != null;
    }

    /**
     * Returns true if an entry exists for the given IP.
     *
     * @param ip the IP address to check; must not be null
     * @return true if a mapping exists
     * @throws IllegalArgumentException if ip is null
     */
    public boolean contains(IPAddress ip) {
        if (ip == null) {
            throw new IllegalArgumentException("IP address must not be null");
        }
        return table.containsKey(ip.getAddress());
    }

    /**
     * Returns an unmodifiable view of all ARP entries.
     *
     * @return unmodifiable map of IP string to MAC string
     */
    public Map<String, String> getEntries() {
        return Collections.unmodifiableMap(table);
    }

    /**
     * Returns the number of entries in the ARP table.
     *
     * @return entry count
     */
    public int size() {
        return table.size();
    }

    /**
     * Removes all entries from the ARP table.
     */
    public void clear() {
        table.clear();
    }
}
package com.filium.model.devices;

import com.filium.model.network.IPAddress;
import com.filium.packet.Packet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a DNS server. Extends Device.
 * Stores a hostname-to-IP mapping table (A records).
 * Responds to DNS query packets with the matching IP or NXDOMAIN.
 * Query handling logic is delegated to DNSHandler in the simulation layer.
 */
public class DNSServer extends Device {

    private final Map<String, IPAddress> records;

    /**
     * Constructs a new DNSServer with the given name.
     *
     * @param name the user-assigned label; must not be null
     */
    public DNSServer(String name) {
        super(name, DeviceType.DNS_SERVER);
        this.records = new HashMap<>();
    }

    /**
     * Adds or updates a DNS A-record mapping.
     *
     * @param hostname the hostname to register; must not be null or blank
     * @param ip       the IP address to associate; must not be null
     * @throws IllegalArgumentException if hostname is null/blank or ip is null
     */
    public void addRecord(String hostname, IPAddress ip) {
        if (hostname == null || hostname.isBlank()) {
            throw new IllegalArgumentException("Hostname must not be null or blank");
        }
        if (ip == null) {
            throw new IllegalArgumentException("IP address must not be null");
        }
        records.put(hostname, ip);
    }

    /**
     * Removes a DNS A-record by hostname.
     *
     * @param hostname the hostname to remove
     * @return true if the record existed and was removed, false if not found
     */
    public boolean removeRecord(String hostname) {
        return records.remove(hostname) != null;
    }

    /**
     * Looks up the IP address for a given hostname.
     *
     * @param hostname the hostname to resolve
     * @return Optional containing the IPAddress if found, empty otherwise
     */
    public Optional<IPAddress> lookup(String hostname) {
        return Optional.ofNullable(records.get(hostname));
    }

    /**
     * Returns an unmodifiable view of all DNS records.
     *
     * @return unmodifiable map of hostname to IPAddress
     */
    public Map<String, IPAddress> getRecords() {
        return Collections.unmodifiableMap(records);
    }

    /**
     * Receives a packet. DNS query handling is delegated to DNSHandler.
     *
     * @param packet the incoming packet; must not be null
     * @throws IllegalArgumentException if packet is null
     */
    @Override
    public void receivePacket(Packet packet) {
        if (packet == null) {
            throw new IllegalArgumentException("Packet must not be null");
        }
        // Query handling is delegated to DNSHandler in the simulation layer
    }

    /**
     * Resets this DNS server by clearing all records.
     */
    @Override
    public void reset() {
        records.clear();
    }
}
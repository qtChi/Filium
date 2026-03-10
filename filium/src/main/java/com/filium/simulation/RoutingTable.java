package com.filium.simulation;

import com.filium.model.network.IPAddress;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Holds static routing table entries for a Router device.
 * Each entry maps a destination network (CIDR notation) to a next-hop IP.
 * Entries are evaluated in insertion order; the first match wins.
 */
public class RoutingTable {

    /**
     * Immutable routing table entry.
     */
    public static final class RouteEntry {
        private final String network;
        private final String cidrMask;
        private final IPAddress nextHop;

        /**
         * @param network  destination network in dotted-decimal (e.g. "192.168.1.0")
         * @param cidrMask CIDR prefix length as string (e.g. "24")
         * @param nextHop  next-hop IP address; must not be null
         */
        public RouteEntry(String network, String cidrMask, IPAddress nextHop) {
            if (network == null || network.isBlank()) {
                throw new IllegalArgumentException("Network must not be null or blank");
            }
            if (cidrMask == null || cidrMask.isBlank()) {
                throw new IllegalArgumentException("CIDR mask must not be null or blank");
            }
            if (nextHop == null) {
                throw new IllegalArgumentException("Next-hop must not be null");
            }
            this.network  = network;
            this.cidrMask = cidrMask;
            this.nextHop  = nextHop;
        }

        public String getNetwork()  { return network; }
        public String getCidrMask() { return cidrMask; }
        public IPAddress getNextHop() { return nextHop; }

        @Override
        public String toString() {
            return network + "/" + cidrMask + " -> " + nextHop;
        }
    }

    private final Map<String, RouteEntry> entries; // key = "network/cidr"

    /** Constructs an empty RoutingTable. */
    public RoutingTable() {
        this.entries = new LinkedHashMap<>();
    }

    /**
     * Adds or replaces a route entry.
     *
     * @param entry the entry to add; must not be null
     * @throws IllegalArgumentException if entry is null
     */
    public void addRoute(RouteEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("RouteEntry must not be null");
        }
        entries.put(key(entry.getNetwork(), entry.getCidrMask()), entry);
    }

    /**
     * Removes the route for the given network/cidr pair.
     *
     * @param network  the destination network
     * @param cidrMask the CIDR prefix length string
     * @return true if a route was removed, false if not found
     */
    public boolean removeRoute(String network, String cidrMask) {
        return entries.remove(key(network, cidrMask)) != null;
    }

    /**
     * Looks up the next-hop for a destination IP by checking each route entry
     * using prefix matching. Returns the first match in insertion order.
     *
     * @param destination the destination IP to look up; must not be null
     * @return Optional containing the next-hop IPAddress, or empty if no route found
     * @throws IllegalArgumentException if destination is null
     */
    public Optional<IPAddress> lookup(IPAddress destination) {
        if (destination == null) {
            throw new IllegalArgumentException("Destination IP must not be null");
        }
        for (RouteEntry entry : entries.values()) {
            if (destination.isInSubnet(
                    new IPAddress(entry.getNetwork()),
                    cidrToMask(entry.getCidrMask()))) {
                return Optional.of(entry.getNextHop());
            }
        }
        return Optional.empty();
    }

    /**
     * Returns an unmodifiable view of all route entries.
     *
     * @return unmodifiable map of key to RouteEntry
     */
    public Map<String, RouteEntry> getEntries() {
        return Collections.unmodifiableMap(entries);
    }

    /**
     * Returns the number of routes in this table.
     *
     * @return route count
     */
    public int size() {
        return entries.size();
    }

    /**
     * Removes all route entries.
     */
    public void clear() {
        entries.clear();
    }

    private String key(String network, String cidr) {
        return network + "/" + cidr;
    }

    /**
     * Converts a CIDR prefix length string (e.g. "24") to a
     * dotted-decimal subnet mask (e.g. "255.255.255.0").
     */
    private String cidrToMask(String cidr) {
        int prefix;
        try {
            prefix = Integer.parseInt(cidr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid CIDR prefix: " + cidr);
        }
        if (prefix < 0 || prefix > 32) {
            throw new IllegalArgumentException("CIDR prefix out of range: " + prefix);
        }
        int mask = prefix == 0 ? 0 : (0xFFFFFFFF << (32 - prefix));
        return ((mask >> 24) & 0xFF) + "."
             + ((mask >> 16) & 0xFF) + "."
             + ((mask >>  8) & 0xFF) + "."
             + ( mask        & 0xFF);
    }
}
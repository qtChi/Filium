package com.filium.model.devices;

import com.filium.packet.Packet;
import com.filium.packet.PacketType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a stateless packet-filtering firewall. Extends Device.
 * Maintains an ordered list of rules evaluated top-to-bottom.
 * Each rule matches on source IP, destination IP, destination port,
 * and protocol. The first matching rule determines the verdict.
 * If no rule matches, the default policy applies.
 */
public class Firewall extends Device {

    /**
     * Immutable record representing a single firewall rule.
     * Null fields act as wildcards — they match any value.
     */
    public static final class FirewallRule {

        private final String srcIP;
        private final String dstIP;
        private final int dstPort;
        private final PacketType protocol;
        private final boolean allow;

        /**
         * Constructs a FirewallRule.
         *
         * @param srcIP    source IP to match, or null for any
         * @param dstIP    destination IP to match, or null for any
         * @param dstPort  destination port to match, or -1 for any
         * @param protocol packet type to match, or null for any
         * @param allow    true to allow matching packets, false to deny
         */
        public FirewallRule(String srcIP, String dstIP,
                            int dstPort, PacketType protocol, boolean allow) {
            this.srcIP = srcIP;
            this.dstIP = dstIP;
            this.dstPort = dstPort;
            this.protocol = protocol;
            this.allow = allow;
        }

        public String getSrcIP()        { return srcIP; }
        public String getDstIP()        { return dstIP; }
        public int getDstPort()         { return dstPort; }
        public PacketType getProtocol() { return protocol; }
        public boolean isAllow()        { return allow; }

        /**
         * Returns true if this rule matches the given packet.
         * Null fields in the rule act as wildcards.
         */
        public boolean matches(Packet packet) {
            if (srcIP != null &&
                !srcIP.equals(packet.getHeader().getSourceIP().getAddress())) {
                return false;
            }
            if (dstIP != null &&
                !dstIP.equals(packet.getHeader().getDestinationIP().getAddress())) {
                return false;
            }
            if (protocol != null && protocol != packet.getType()) {
                return false;
            }
            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FirewallRule r)) return false;
            return dstPort == r.dstPort
                && allow == r.allow
                && Objects.equals(srcIP, r.srcIP)
                && Objects.equals(dstIP, r.dstIP)
                && protocol == r.protocol;
        }

        @Override
        public int hashCode() {
            return Objects.hash(srcIP, dstIP, dstPort, protocol, allow);
        }

        @Override
        public String toString() {
            return "FirewallRule{src=" + srcIP + ", dst=" + dstIP
                + ", port=" + dstPort + ", proto=" + protocol
                + ", " + (allow ? "ALLOW" : "DENY") + "}";
        }
    }

    private final List<FirewallRule> rules;
    private boolean defaultAllow;

    /**
     * Constructs a new Firewall with the given name.
     * Default policy is DENY (defaultAllow = false).
     *
     * @param name the user-assigned label; must not be null
     */
    public Firewall(String name) {
        super(name, DeviceType.FIREWALL);
        this.rules = new ArrayList<>();
        this.defaultAllow = false;
    }

    /**
     * Appends a rule to the end of the rule list.
     *
     * @param rule the rule to add; must not be null
     * @throws IllegalArgumentException if rule is null
     */
    public void addRule(FirewallRule rule) {
        if (rule == null) {
            throw new IllegalArgumentException("FirewallRule must not be null");
        }
        rules.add(rule);
    }

    /**
     * Removes the rule at the given index.
     *
     * @param index the zero-based index of the rule to remove
     * @throws IndexOutOfBoundsException if index is out of range
     */
    public void removeRule(int index) {
        if (index < 0 || index >= rules.size()) {
            throw new IndexOutOfBoundsException(
                "Rule index " + index + " is out of range [0, " + rules.size() + ")");
        }
        rules.remove(index);
    }

    /**
     * Returns an unmodifiable view of the rule list.
     *
     * @return unmodifiable list of FirewallRule
     */
    public List<FirewallRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    /**
     * Returns the default allow policy.
     *
     * @return true if unmatched packets are allowed, false if denied
     */
    public boolean isDefaultAllow() {
        return defaultAllow;
    }

    /**
     * Sets the default policy for packets that match no rule.
     *
     * @param defaultAllow true to allow unmatched packets, false to deny
     */
    public void setDefaultAllow(boolean defaultAllow) {
        this.defaultAllow = defaultAllow;
    }

    /**
     * Evaluates a packet against the ordered rule list.
     * Returns the decision of the first matching rule.
     * Returns the default policy if no rule matches.
     *
     * @param packet the packet to evaluate; must not be null
     * @return true if the packet should be allowed, false if it should be dropped
     * @throws IllegalArgumentException if packet is null
     */
    public boolean evaluate(Packet packet) {
        if (packet == null) {
            throw new IllegalArgumentException("Packet must not be null");
        }
        for (FirewallRule rule : rules) {
            if (rule.matches(packet)) {
                return rule.isAllow();
            }
        }
        return defaultAllow;
    }

    /**
     * Receives a packet and applies firewall evaluation.
     * Allowed packets pass through; denied packets are silently dropped.
     *
     * @param packet the incoming packet; must not be null
     * @throws IllegalArgumentException if packet is null
     */
    @Override
    public void receivePacket(Packet packet) {
        if (packet == null) {
            throw new IllegalArgumentException("Packet must not be null");
        }
        evaluate(packet);
        // Forwarding of allowed packets is handled by SimulationEngine
    }

    /**
     * Resets this firewall by clearing all rules and
     * restoring the default deny policy.
     */
    @Override
    public void reset() {
        rules.clear();
        defaultAllow = false;
    }
}
package com.filium.simulation;

/**
 * Enumerates all simulation event types emitted by the SimulationEngine.
 * Used by the event bus to drive both the simulation log panel and packet animations.
 */
public enum SimulationEventType {

    // Packet lifecycle
    PACKET_SENT,
    PACKET_RECEIVED,
    PACKET_DROPPED,
    PACKET_TTL_EXPIRED,

    // Protocol resolution
    DNS_RESOLVED,
    DNS_FAILED,
    DHCP_ASSIGNED,
    ARP_RESOLVED,

    // Firewall
    FIREWALL_BLOCKED,
    FIREWALL_ALLOWED
}
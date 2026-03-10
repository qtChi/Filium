package com.filium.packet;

/**
 * Enumerates all packet and protocol types supported by the Filium simulation engine.
 * Each value represents a distinct packet kind that a protocol handler can process.
 */
public enum PacketType {

    // ICMP
    ICMP_ECHO_REQUEST,
    ICMP_ECHO_REPLY,
    ICMP_UNREACHABLE,

    // ARP
    ARP_REQUEST,
    ARP_REPLY,

    // DNS
    DNS_QUERY,
    DNS_RESPONSE,

    // DHCP
    DHCP_DISCOVER,
    DHCP_OFFER,
    DHCP_REQUEST,
    DHCP_ACK,

    // HTTP
    HTTP_REQUEST,
    HTTP_RESPONSE
}
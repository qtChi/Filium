package com.filium.simulation.protocols;

import com.filium.model.devices.DNSServer;
import com.filium.model.devices.Device;
import com.filium.model.network.IPAddress;
import com.filium.model.network.NetworkTopology;
import com.filium.packet.Packet;
import com.filium.packet.PacketHeader;
import com.filium.packet.PacketType;
import com.filium.simulation.PacketQueue;
import com.filium.simulation.SimulationEvent;
import com.filium.simulation.SimulationEventType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Handles DNS_QUERY packets.
 * Finds the DNS server in the topology, looks up the queried hostname,
 * enqueues a DNS_RESPONSE, and emits DNS_RESOLVED or DNS_FAILED.
 */
public class DNSHandler implements Protocol {

    @Override
    public boolean canHandle(Packet packet) {
        return packet.getType() == PacketType.DNS_QUERY
            || packet.getType() == PacketType.DNS_RESPONSE;
    }

    @Override
    public List<SimulationEvent> handle(Packet packet,
                                        NetworkTopology topology,
                                        PacketQueue queue) {
        List<SimulationEvent> events = new ArrayList<>();

        if (packet.getType() == PacketType.DNS_RESPONSE) {
            // Responses are delivered to the client — just emit received event
            events.add(new SimulationEvent(
                SimulationEventType.PACKET_RECEIVED,
                null, null, packet,
                "DNS response received: " + packet.getPayload()));
            return events;
        }

        // Find a DNS server in the topology
        DNSServer dnsServer = null;
        for (Device d : topology.getDevices()) {
            if (d instanceof DNSServer ds) {
                dnsServer = ds;
                break;
            }
        }

        if (dnsServer == null || dnsServer.getNetworkInterface().getIpAddress() == null) {
            events.add(new SimulationEvent(
                SimulationEventType.DNS_FAILED,
                null, null, packet,
                "DNS query failed — no DNS server in topology"));
            return events;
        }

        String hostname = packet.getPayload();
        Optional<IPAddress> resolved = dnsServer.lookup(hostname);

        if (resolved.isPresent()) {
            PacketHeader responseHeader = new PacketHeader(
                dnsServer.getNetworkInterface().getIpAddress(),
                packet.getHeader().getSourceIP(),
                dnsServer.getNetworkInterface().getMacAddress(),
                packet.getHeader().getSourceMAC(),
                64);
            queue.enqueue(new Packet(PacketType.DNS_RESPONSE,
                responseHeader, resolved.get().getAddress()));

            events.add(new SimulationEvent(
                SimulationEventType.DNS_RESOLVED,
                dnsServer, null, packet,
                "DNS resolved: " + hostname + " -> " + resolved.get()));
        } else {
            events.add(new SimulationEvent(
                SimulationEventType.DNS_FAILED,
                dnsServer, null, packet,
                "DNS failed: no record for '" + hostname + "'"));
        }

        return events;
    }
}
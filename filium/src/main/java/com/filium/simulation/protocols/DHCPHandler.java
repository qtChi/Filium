package com.filium.simulation.protocols;

import com.filium.model.devices.DHCPServer;
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
 * Handles DHCP_DISCOVER and DHCP_REQUEST packets.
 * Finds the DHCP server in the topology, assigns an IP, and enqueues
 * DHCP_OFFER / DHCP_ACK responses.
 */
public class DHCPHandler implements Protocol {

    @Override
    public boolean canHandle(Packet packet) {
        return packet.getType() == PacketType.DHCP_DISCOVER
            || packet.getType() == PacketType.DHCP_REQUEST
            || packet.getType() == PacketType.DHCP_OFFER
            || packet.getType() == PacketType.DHCP_ACK;
    }

    @Override
    public List<SimulationEvent> handle(Packet packet,
                                        NetworkTopology topology,
                                        PacketQueue queue) {
        List<SimulationEvent> events = new ArrayList<>();

        if (packet.getType() == PacketType.DHCP_OFFER
            || packet.getType() == PacketType.DHCP_ACK) {
            events.add(new SimulationEvent(
                SimulationEventType.PACKET_RECEIVED,
                null, null, packet,
                "DHCP " + packet.getType() + " received"));
            return events;
        }

        // Find DHCP server
        DHCPServer dhcpServer = null;
        for (Device d : topology.getDevices()) {
            if (d instanceof DHCPServer ds) {
                dhcpServer = ds;
                break;
            }
        }

        if (dhcpServer == null || dhcpServer.getNetworkInterface().getIpAddress() == null) {
            events.add(new SimulationEvent(
                SimulationEventType.PACKET_DROPPED,
                null, null, packet,
                "DHCP " + packet.getType() + " dropped — no DHCP server in topology"));
            return events;
        }

        String clientMAC = packet.getHeader().getSourceMAC();
        Optional<IPAddress> assigned = dhcpServer.assignIP(clientMAC);

        if (assigned.isPresent()) {
            PacketType responseType = packet.getType() == PacketType.DHCP_DISCOVER
                ? PacketType.DHCP_OFFER : PacketType.DHCP_ACK;

            PacketHeader responseHeader = new PacketHeader(
                dhcpServer.getNetworkInterface().getIpAddress(),
                packet.getHeader().getSourceIP(),
                dhcpServer.getNetworkInterface().getMacAddress(),
                packet.getHeader().getSourceMAC(),
                64);
            queue.enqueue(new Packet(responseType, responseHeader,
                assigned.get().getAddress()));

            events.add(new SimulationEvent(
                SimulationEventType.DHCP_ASSIGNED,
                dhcpServer, null, packet,
                "DHCP assigned " + assigned.get() + " to " + clientMAC));
        } else {
            events.add(new SimulationEvent(
                SimulationEventType.PACKET_DROPPED,
                dhcpServer, null, packet,
                "DHCP pool exhausted — cannot assign IP to " + clientMAC));
        }

        return events;
    }
}
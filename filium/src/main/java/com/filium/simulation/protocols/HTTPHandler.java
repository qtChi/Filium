package com.filium.simulation.protocols;

import com.filium.model.devices.Device;
import com.filium.model.network.NetworkTopology;
import com.filium.packet.Packet;
import com.filium.packet.PacketHeader;
import com.filium.packet.PacketType;
import com.filium.simulation.PacketQueue;
import com.filium.simulation.SimulationEvent;
import com.filium.simulation.SimulationEventType;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles HTTP_REQUEST and HTTP_RESPONSE packets.
 * On request: locates the destination device and enqueues a 200 OK response.
 * On response: emits PACKET_RECEIVED.
 */
public class HTTPHandler implements Protocol {

    private static final String DEFAULT_RESPONSE = "HTTP/1.1 200 OK";

    @Override
    public boolean canHandle(Packet packet) {
        return packet.getType() == PacketType.HTTP_REQUEST
            || packet.getType() == PacketType.HTTP_RESPONSE;
    }

    @Override
    public List<SimulationEvent> handle(Packet packet,
                                        NetworkTopology topology,
                                        PacketQueue queue) {
        List<SimulationEvent> events = new ArrayList<>();

        if (packet.getType() == PacketType.HTTP_RESPONSE) {
            Device dst = null;
            for (Device d : topology.getDevices()) {
                if (d.getNetworkInterface().getIpAddress() != null
                    && d.getNetworkInterface().getIpAddress()
                        .equals(packet.getHeader().getDestinationIP())) {
                    dst = d;
                    break;
                }
            }
            events.add(new SimulationEvent(
                SimulationEventType.PACKET_RECEIVED,
                null, dst, packet,
                "HTTP response received"
                    + (dst != null ? " by " + dst.getName() : "")));
            return events;
        }

        // HTTP_REQUEST — find the server
        Device server = null;
        for (Device d : topology.getDevices()) {
            if (d.getNetworkInterface().getIpAddress() != null
                && d.getNetworkInterface().getIpAddress()
                    .equals(packet.getHeader().getDestinationIP())) {
                server = d;
                break;
            }
        }

        Device client = null;
        for (Device d : topology.getDevices()) {
            if (d.getNetworkInterface().getIpAddress() != null
                && d.getNetworkInterface().getIpAddress()
                    .equals(packet.getHeader().getSourceIP())) {
                client = d;
                break;
            }
        }

        if (server != null) {
            PacketHeader responseHeader = new PacketHeader(
                packet.getHeader().getDestinationIP(),
                packet.getHeader().getSourceIP(),
                packet.getHeader().getDestinationMAC(),
                packet.getHeader().getSourceMAC(),
                64);
            queue.enqueue(new Packet(PacketType.HTTP_RESPONSE,
                responseHeader, DEFAULT_RESPONSE));

            events.add(new SimulationEvent(
                SimulationEventType.PACKET_RECEIVED,
                client, server, packet,
                "HTTP request received by " + server.getName()));
        } else {
            events.add(new SimulationEvent(
                SimulationEventType.PACKET_DROPPED,
                client, null, packet,
                "HTTP request dropped — server not found"));
        }

        return events;
    }
}
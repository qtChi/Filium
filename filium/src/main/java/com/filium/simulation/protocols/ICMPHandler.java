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
import java.util.Optional;

/**
 * Handles ICMP_ECHO_REQUEST and ICMP_ECHO_REPLY packets.
 * On receiving a request destined for a known device, enqueues a reply.
 * On receiving a reply, emits a PACKET_RECEIVED event.
 */
public class ICMPHandler implements Protocol {

    @Override
    public boolean canHandle(Packet packet) {
        return packet.getType() == PacketType.ICMP_ECHO_REQUEST
            || packet.getType() == PacketType.ICMP_ECHO_REPLY;
    }

    @Override
    public List<SimulationEvent> handle(Packet packet,
                                        NetworkTopology topology,
                                        PacketQueue queue) {
        List<SimulationEvent> events = new ArrayList<>();

        if (packet.getType() == PacketType.ICMP_ECHO_REQUEST) {
            Optional<Device> target = topology.findDeviceById(
                packet.getHeader().getDestinationIP().getAddress());

            // Find destination device by IP
            Device dst = null;
            for (Device d : topology.getDevices()) {
                if (d.getNetworkInterface().getIpAddress() != null
                    && d.getNetworkInterface().getIpAddress()
                        .equals(packet.getHeader().getDestinationIP())) {
                    dst = d;
                    break;
                }
            }

            Device src = null;
            for (Device d : topology.getDevices()) {
                if (d.getNetworkInterface().getIpAddress() != null
                    && d.getNetworkInterface().getIpAddress()
                        .equals(packet.getHeader().getSourceIP())) {
                    src = d;
                    break;
                }
            }

            if (dst != null) {
                // Build and enqueue a reply
                PacketHeader replyHeader = new PacketHeader(
                    packet.getHeader().getDestinationIP(),
                    packet.getHeader().getSourceIP(),
                    packet.getHeader().getDestinationMAC(),
                    packet.getHeader().getSourceMAC(),
                    64);
                Packet reply = new Packet(PacketType.ICMP_ECHO_REPLY,
                    replyHeader, packet.getPayload());
                queue.enqueue(reply);

                events.add(new SimulationEvent(
                    SimulationEventType.PACKET_RECEIVED,
                    src, dst, packet,
                    "ICMP echo request received by " + dst.getName()));
            } else {
                events.add(new SimulationEvent(
                    SimulationEventType.PACKET_DROPPED,
                    src, null, packet,
                    "ICMP echo request dropped — destination unreachable"));
            }

        } else {
            // ICMP_ECHO_REPLY
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
                "ICMP echo reply received"
                    + (dst != null ? " by " + dst.getName() : "")));
        }

        return events;
    }
}
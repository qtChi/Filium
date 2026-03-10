package com.filium.simulation.protocols;

import com.filium.model.devices.Device;
import com.filium.model.network.NetworkTopology;
import com.filium.packet.Packet;
import com.filium.packet.PacketHeader;
import com.filium.packet.PacketType;
import com.filium.simulation.ARPTable;
import com.filium.simulation.PacketQueue;
import com.filium.simulation.SimulationEvent;
import com.filium.simulation.SimulationEventType;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles ARP_REQUEST and ARP_REPLY packets.
 * On request: records the sender in the ARP table and, if the target IP
 * belongs to a known device, enqueues a reply and emits ARP_RESOLVED.
 * On reply: records the sender in the ARP table.
 */
public class ARPHandler implements Protocol {

    private final ARPTable arpTable;

    /**
     * Constructs an ARPHandler backed by the given ARPTable.
     *
     * @param arpTable the shared ARP table; must not be null
     * @throws IllegalArgumentException if arpTable is null
     */
    public ARPHandler(ARPTable arpTable) {
        if (arpTable == null) {
            throw new IllegalArgumentException("ARPTable must not be null");
        }
        this.arpTable = arpTable;
    }

    @Override
    public boolean canHandle(Packet packet) {
        return packet.getType() == PacketType.ARP_REQUEST
            || packet.getType() == PacketType.ARP_REPLY;
    }

    @Override
    public List<SimulationEvent> handle(Packet packet,
                                        NetworkTopology topology,
                                        PacketQueue queue) {
        List<SimulationEvent> events = new ArrayList<>();

        // Learn the sender regardless of request or reply
        arpTable.put(packet.getHeader().getSourceIP(),
                     packet.getHeader().getSourceMAC());

        if (packet.getType() == PacketType.ARP_REQUEST) {
            // Find the device that owns the target IP
            Device target = null;
            for (Device d : topology.getDevices()) {
                if (d.getNetworkInterface().getIpAddress() != null
                    && d.getNetworkInterface().getIpAddress()
                        .equals(packet.getHeader().getDestinationIP())) {
                    target = d;
                    break;
                }
            }

            if (target != null) {
                String targetMAC = target.getNetworkInterface().getMacAddress();
                arpTable.put(packet.getHeader().getDestinationIP(), targetMAC);

                PacketHeader replyHeader = new PacketHeader(
                    packet.getHeader().getDestinationIP(),
                    packet.getHeader().getSourceIP(),
                    targetMAC,
                    packet.getHeader().getSourceMAC(),
                    64);
                queue.enqueue(new Packet(PacketType.ARP_REPLY, replyHeader, targetMAC));

                events.add(new SimulationEvent(
                    SimulationEventType.ARP_RESOLVED,
                    null, target, packet,
                    "ARP resolved: " + packet.getHeader().getDestinationIP()
                        + " is at " + targetMAC));
            } else {
                events.add(new SimulationEvent(
                    SimulationEventType.PACKET_DROPPED,
                    null, null, packet,
                    "ARP request dropped — target IP not found in topology"));
            }
        }

        return events;
    }
}
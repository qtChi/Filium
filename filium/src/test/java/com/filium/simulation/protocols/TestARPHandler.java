package com.filium.simulation.protocols;

import com.filium.model.devices.PC;
import com.filium.model.network.IPAddress;
import com.filium.model.network.NetworkTopology;
import com.filium.packet.Packet;
import com.filium.packet.PacketHeader;
import com.filium.packet.PacketType;
import com.filium.simulation.ARPTable;
import com.filium.simulation.PacketQueue;
import com.filium.simulation.SimulationEvent;
import com.filium.simulation.SimulationEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestARPHandler {

    private static final String MAC_A = "AA:BB:CC:DD:EE:FF";
    private static final String MAC_B = "11:22:33:44:55:66";

    private ARPHandler handler;
    private ARPTable arpTable;
    private NetworkTopology topology;
    private PacketQueue queue;
    private PC pcA, pcB;

    @BeforeEach void setUp() {
        arpTable = new ARPTable();
        handler  = new ARPHandler(arpTable);
        topology = new NetworkTopology();
        queue    = new PacketQueue();
        pcA = new PC("PC-A"); pcA.getNetworkInterface().setIpAddress(new IPAddress("10.0.0.1"));
        pcB = new PC("PC-B"); pcB.getNetworkInterface().setIpAddress(new IPAddress("10.0.0.2"));
        topology.addDevice(pcA); topology.addDevice(pcB);
    }

    private Packet arpRequest() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("10.0.0.2"), MAC_A, MAC_B, 64);
        return new Packet(PacketType.ARP_REQUEST, h, "");
    }

    private Packet arpReply() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.2"), new IPAddress("10.0.0.1"), MAC_B, MAC_A, 64);
        return new Packet(PacketType.ARP_REPLY, h, MAC_B);
    }

    // constructor
    @Test void constructor_nullARPTable_throws() {
        assertThrows(IllegalArgumentException.class, () -> new ARPHandler(null));
    }

    // canHandle
    @Test void canHandle_arpRequest_returnsTrue() { assertTrue(handler.canHandle(arpRequest())); }
    @Test void canHandle_arpReply_returnsTrue()   { assertTrue(handler.canHandle(arpReply())); }
    @Test void canHandle_icmpPacket_returnsFalse() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("10.0.0.2"), MAC_A, MAC_B, 64);
        assertFalse(handler.canHandle(new Packet(PacketType.ICMP_ECHO_REQUEST, h, "")));
    }

    // handle — request with known target
    @Test void handle_request_learnsSenderInARPTable() {
        handler.handle(arpRequest(), topology, queue);
        assertTrue(arpTable.contains(new IPAddress("10.0.0.1")));
    }
    @Test void handle_requestToKnownTarget_enqueuesReply() {
        handler.handle(arpRequest(), topology, queue);
        assertFalse(queue.isEmpty());
        assertEquals(PacketType.ARP_REPLY, queue.dequeue().orElseThrow().getType());
    }
    @Test void handle_requestToKnownTarget_emitsARPResolvedEvent() {
        List<SimulationEvent> events = handler.handle(arpRequest(), topology, queue);
        assertEquals(SimulationEventType.ARP_RESOLVED, events.get(0).getType());
    }

    // handle — request with unknown target
    @Test void handle_requestToUnknownTarget_noReplyEnqueued() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("9.9.9.9"), MAC_A, MAC_B, 64);
        Packet p = new Packet(PacketType.ARP_REQUEST, h, "");
        handler.handle(p, topology, queue);
        assertTrue(queue.isEmpty());
    }
    @Test void handle_requestToUnknownTarget_emitsDroppedEvent() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("9.9.9.9"), MAC_A, MAC_B, 64);
        Packet p = new Packet(PacketType.ARP_REQUEST, h, "");
        List<SimulationEvent> events = handler.handle(p, topology, queue);
        assertEquals(SimulationEventType.PACKET_DROPPED, events.get(0).getType());
    }

    // handle — reply
    @Test void handle_reply_learnsSenderInARPTable() {
        handler.handle(arpReply(), topology, queue);
        assertTrue(arpTable.contains(new IPAddress("10.0.0.2")));
    }
    @Test void handle_reply_returnsNoEvents() {
        List<SimulationEvent> events = handler.handle(arpReply(), topology, queue);
        assertTrue(events.isEmpty());
    }
}
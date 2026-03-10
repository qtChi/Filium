package com.filium.simulation.protocols;

import com.filium.model.devices.PC;
import com.filium.model.network.IPAddress;
import com.filium.model.network.NetworkTopology;
import com.filium.packet.Packet;
import com.filium.packet.PacketHeader;
import com.filium.packet.PacketType;
import com.filium.simulation.PacketQueue;
import com.filium.simulation.SimulationEvent;
import com.filium.simulation.SimulationEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TestHTTPHandler {

    private static final String MAC_A = "AA:BB:CC:DD:EE:FF";
    private static final String MAC_B = "11:22:33:44:55:66";

    private HTTPHandler handler;
    private NetworkTopology topology;
    private PacketQueue queue;
    private PC client, server;

    @BeforeEach void setUp() {
        handler  = new HTTPHandler();
        topology = new NetworkTopology();
        queue    = new PacketQueue();
        client = new PC("Client"); client.getNetworkInterface().setIpAddress(new IPAddress("10.0.0.1"));
        server = new PC("Server"); server.getNetworkInterface().setIpAddress(new IPAddress("10.0.0.80"));
        topology.addDevice(client); topology.addDevice(server);
    }

    private Packet httpRequest() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("10.0.0.80"), MAC_A, MAC_B, 64);
        return new Packet(PacketType.HTTP_REQUEST, h, "GET / HTTP/1.1");
    }

    private Packet httpResponse() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.80"), new IPAddress("10.0.0.1"), MAC_B, MAC_A, 64);
        return new Packet(PacketType.HTTP_RESPONSE, h, "HTTP/1.1 200 OK");
    }

    // canHandle
    @Test void canHandle_httpRequest_returnsTrue()  { assertTrue(handler.canHandle(httpRequest())); }
    @Test void canHandle_httpResponse_returnsTrue() { assertTrue(handler.canHandle(httpResponse())); }
    @Test void canHandle_icmp_returnsFalse() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("10.0.0.2"), MAC_A, MAC_B, 64);
        assertFalse(handler.canHandle(new Packet(PacketType.ICMP_ECHO_REQUEST, h, "")));
    }

    // handle — request with known server
    @Test void handle_requestToKnownServer_enqueuesResponse() {
        handler.handle(httpRequest(), topology, queue);
        assertFalse(queue.isEmpty());
        assertEquals(PacketType.HTTP_RESPONSE, queue.dequeue().orElseThrow().getType());
    }
    @Test void handle_requestToKnownServer_emitsReceivedEvent() {
        List<SimulationEvent> events = handler.handle(httpRequest(), topology, queue);
        assertEquals(SimulationEventType.PACKET_RECEIVED, events.get(0).getType());
    }

    // handle — request with unknown server
    @Test void handle_requestToUnknownServer_doesNotEnqueueResponse() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("9.9.9.9"), MAC_A, MAC_B, 64);
        handler.handle(new Packet(PacketType.HTTP_REQUEST, h, "GET /"), topology, queue);
        assertTrue(queue.isEmpty());
    }
    @Test void handle_requestToUnknownServer_emitsDroppedEvent() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("9.9.9.9"), MAC_A, MAC_B, 64);
        List<SimulationEvent> events = handler.handle(
            new Packet(PacketType.HTTP_REQUEST, h, "GET /"), topology, queue);
        assertEquals(SimulationEventType.PACKET_DROPPED, events.get(0).getType());
    }

    // handle — response
    @Test void handle_response_emitsReceivedEvent() {
        List<SimulationEvent> events = handler.handle(httpResponse(), topology, queue);
        assertEquals(SimulationEventType.PACKET_RECEIVED, events.get(0).getType());
    }
    @Test void handle_response_doesNotEnqueuePacket() {
        handler.handle(httpResponse(), topology, queue);
        assertTrue(queue.isEmpty());
    }
    @Test void handle_responseToUnknownDst_emitsReceivedEventWithNullDst() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.80"), new IPAddress("9.9.9.9"), MAC_B, MAC_A, 64);
        List<SimulationEvent> events = handler.handle(
            new Packet(PacketType.HTTP_RESPONSE, h, "200 OK"), topology, queue);
        assertEquals(SimulationEventType.PACKET_RECEIVED, events.get(0).getType());
        assertNull(events.get(0).getDestination());
    }
}
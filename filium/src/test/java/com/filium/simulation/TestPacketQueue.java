package com.filium.simulation;

import com.filium.model.network.IPAddress;
import com.filium.packet.Packet;
import com.filium.packet.PacketHeader;
import com.filium.packet.PacketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestPacketQueue {

    private static final String MAC_A = "AA:BB:CC:DD:EE:FF";
    private static final String MAC_B = "11:22:33:44:55:66";

    private PacketQueue queue;

    @BeforeEach void setUp() { queue = new PacketQueue(); }

    private Packet pkt() {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("10.0.0.2"), MAC_A, MAC_B, 64);
        return new Packet(PacketType.ICMP_ECHO_REQUEST, h, "");
    }

    @Test void constructor_isEmpty() { assertTrue(queue.isEmpty()); }
    @Test void constructor_sizeZero() { assertEquals(0, queue.size()); }

    @Test void enqueue_null_throws() {
        assertThrows(IllegalArgumentException.class, () -> queue.enqueue(null));
    }

    @Test void enqueue_validPacket_sizeIncreases() {
        queue.enqueue(pkt());
        assertEquals(1, queue.size());
    }

    @Test void enqueue_multiplePackets_fifoOrder() {
        Packet first = pkt(); Packet second = pkt();
        queue.enqueue(first); queue.enqueue(second);
        assertEquals(first, queue.dequeue().orElseThrow());
        assertEquals(second, queue.dequeue().orElseThrow());
    }

    @Test void dequeue_emptyQueue_returnsEmpty() {
        assertTrue(queue.dequeue().isEmpty());
    }

    @Test void dequeue_nonEmpty_removesAndReturnsHead() {
        Packet p = pkt(); queue.enqueue(p);
        assertEquals(p, queue.dequeue().orElseThrow());
        assertTrue(queue.isEmpty());
    }

    @Test void peek_emptyQueue_returnsEmpty() {
        assertTrue(queue.peek().isEmpty());
    }

    @Test void peek_nonEmpty_returnsHeadWithoutRemoving() {
        Packet p = pkt(); queue.enqueue(p);
        assertEquals(p, queue.peek().orElseThrow());
        assertEquals(1, queue.size());
    }

    @Test void isEmpty_afterEnqueueDequeue_returnsTrue() {
        queue.enqueue(pkt()); queue.dequeue();
        assertTrue(queue.isEmpty());
    }

    @Test void clear_removesAllPackets() {
        queue.enqueue(pkt()); queue.enqueue(pkt());
        queue.clear();
        assertTrue(queue.isEmpty());
    }
}
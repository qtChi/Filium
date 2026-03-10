package com.filium.simulation;

import com.filium.packet.Packet;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Thread-safe FIFO queue of packets awaiting processing by the SimulationEngine.
 */
public class PacketQueue {

    private final Deque<Packet> queue;

    /** Constructs an empty PacketQueue. */
    public PacketQueue() {
        this.queue = new ArrayDeque<>();
    }

    /**
     * Enqueues a packet at the tail of the queue.
     *
     * @param packet the packet to enqueue; must not be null
     * @throws IllegalArgumentException if packet is null
     */
    public void enqueue(Packet packet) {
        if (packet == null) {
            throw new IllegalArgumentException("Packet must not be null");
        }
        queue.addLast(packet);
    }

    /**
     * Removes and returns the packet at the head of the queue.
     *
     * @return Optional containing the next Packet, or empty if the queue is empty
     */
    public Optional<Packet> dequeue() {
        return Optional.ofNullable(queue.pollFirst());
    }

    /**
     * Returns but does not remove the packet at the head of the queue.
     *
     * @return Optional containing the next Packet, or empty if the queue is empty
     */
    public Optional<Packet> peek() {
        return Optional.ofNullable(queue.peekFirst());
    }

    /**
     * Returns true if the queue contains no packets.
     *
     * @return true if empty
     */
    public boolean isEmpty() {
        return queue.isEmpty();
    }

    /**
     * Returns the number of packets currently in the queue.
     *
     * @return queue size
     */
    public int size() {
        return queue.size();
    }

    /**
     * Removes all packets from the queue.
     */
    public void clear() {
        queue.clear();
    }
}
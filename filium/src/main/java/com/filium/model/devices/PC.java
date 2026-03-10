package com.filium.model.devices;

import com.filium.packet.Packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents an end-user PC. Extends Device.
 * Can send and receive packets. Maintains an inbox of received packets
 * for inspection during and after simulation.
 */
public class PC extends Device {

    private final List<Packet> receivedPackets;

    /**
     * Constructs a new PC with the given name.
     *
     * @param name the user-assigned label; must not be null
     */
    public PC(String name) {
        super(name, DeviceType.PC);
        this.receivedPackets = new ArrayList<>();
    }

    /**
     * Receives a packet and stores it in the inbox.
     *
     * @param packet the incoming packet; must not be null
     * @throws IllegalArgumentException if packet is null
     */
    @Override
    public void receivePacket(Packet packet) {
        if (packet == null) {
            throw new IllegalArgumentException("Packet must not be null");
        }
        receivedPackets.add(packet);
    }

    /**
     * Returns an unmodifiable view of all packets received by this PC.
     *
     * @return unmodifiable list of received packets
     */
    public List<Packet> getReceivedPackets() {
        return Collections.unmodifiableList(receivedPackets);
    }

    /**
     * Resets this PC by clearing the received packets inbox.
     */
    @Override
    public void reset() {
        receivedPackets.clear();
    }
}
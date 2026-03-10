package com.filium.model.devices;

import com.filium.model.network.NetworkInterface;
import com.filium.packet.Packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Layer 2 network switch. Extends Device.
 * Learns source MAC addresses and associates them with the port they arrived on.
 * Forwards frames to known destinations; floods to all ports for unknown MACs.
 * Switching logic is delegated to the SimulationEngine.
 */
public class Switch extends Device {

    public static final int MAX_PORTS = 16;

    private final Map<String, NetworkInterface> macTable;
    private final List<NetworkInterface> ports;

    /**
     * Constructs a new Switch with the given name.
     *
     * @param name the user-assigned label; must not be null
     */
    public Switch(String name) {
        super(name, DeviceType.SWITCH);
        this.macTable = new HashMap<>();
        this.ports = new ArrayList<>();
        this.ports.add(getNetworkInterface());
    }

    /**
     * Adds a port (network interface) to this switch.
     *
     * @param port the interface to add; must not be null
     * @throws IllegalArgumentException if port is null
     * @throws IllegalStateException    if the switch already has MAX_PORTS ports
     */
    public void addPort(NetworkInterface port) {
        if (port == null) {
            throw new IllegalArgumentException("Port must not be null");
        }
        if (ports.size() >= MAX_PORTS) {
            throw new IllegalStateException(
                "Switch already has the maximum of " + MAX_PORTS + " ports");
        }
        ports.add(port);
    }

    /**
     * Returns an unmodifiable view of all ports on this switch.
     *
     * @return unmodifiable list of NetworkInterface
     */
    public List<NetworkInterface> getPorts() {
        return Collections.unmodifiableList(ports);
    }

    /**
     * Learns a MAC address to port mapping.
     *
     * @param mac  the MAC address string; must not be null or blank
     * @param port the port this MAC was learned on; must not be null
     * @throws IllegalArgumentException if mac is null/blank or port is null
     */
    public void learnMAC(String mac, NetworkInterface port) {
        if (mac == null || mac.isBlank()) {
            throw new IllegalArgumentException("MAC address must not be null or blank");
        }
        if (port == null) {
            throw new IllegalArgumentException("Port must not be null");
        }
        macTable.put(mac, port);
    }

    /**
     * Looks up the port associated with a given MAC address.
     *
     * @param mac the MAC address to look up
     * @return the associated NetworkInterface, or null if not found
     */
    public NetworkInterface lookupMAC(String mac) {
        return macTable.get(mac);
    }

    /**
     * Returns an unmodifiable view of the MAC address table.
     *
     * @return unmodifiable map of MAC to NetworkInterface
     */
    public Map<String, NetworkInterface> getMacTable() {
        return Collections.unmodifiableMap(macTable);
    }

    /**
     * Receives a packet. Switching logic is handled by the SimulationEngine.
     *
     * @param packet the incoming packet; must not be null
     * @throws IllegalArgumentException if packet is null
     */
    @Override
    public void receivePacket(Packet packet) {
        if (packet == null) {
            throw new IllegalArgumentException("Packet must not be null");
        }
        // Forwarding logic is handled by SimulationEngine
    }

    /**
     * Resets this switch by flushing the MAC address table
     * and clearing all additional ports, retaining only the base interface.
     */
    @Override
    public void reset() {
        macTable.clear();
        ports.clear();
        ports.add(getNetworkInterface());
    }
}
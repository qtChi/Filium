package com.filium.model.devices;

import com.filium.model.network.NetworkInterface;
import com.filium.packet.Packet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a network router. Extends Device.
 * Supports multiple network interfaces and forwards packets
 * based on destination IP. Routing logic is delegated to the
 * SimulationEngine — this class manages interfaces and state only.
 */
public class Router extends Device {

    public static final int MAX_INTERFACES = 8;

    private final List<NetworkInterface> interfaces;

    /**
     * Constructs a new Router with the given name.
     *
     * @param name the user-assigned label; must not be null
     */
    public Router(String name) {
        super(name, DeviceType.ROUTER);
        this.interfaces = new ArrayList<>();
        this.interfaces.add(getNetworkInterface());
    }

    /**
     * Adds a network interface to this router.
     *
     * @param networkInterface the interface to add; must not be null
     * @throws IllegalArgumentException if networkInterface is null
     * @throws IllegalStateException    if the router already has MAX_INTERFACES interfaces
     */
    public void addInterface(NetworkInterface networkInterface) {
        if (networkInterface == null) {
            throw new IllegalArgumentException("NetworkInterface must not be null");
        }
        if (interfaces.size() >= MAX_INTERFACES) {
            throw new IllegalStateException(
                "Router already has the maximum of " + MAX_INTERFACES + " interfaces");
        }
        interfaces.add(networkInterface);
    }

    /**
     * Returns an unmodifiable view of all network interfaces on this router.
     *
     * @return unmodifiable list of NetworkInterface
     */
    public List<NetworkInterface> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    /**
     * Receives a packet. Routing logic is handled by the SimulationEngine.
     * This method stores state changes only — subclasses or the engine
     * will handle forwarding.
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
     * Resets this router by clearing all additional interfaces,
     * retaining only the base interface from the parent Device.
     */
    @Override
    public void reset() {
        interfaces.clear();
        interfaces.add(getNetworkInterface());
    }
}
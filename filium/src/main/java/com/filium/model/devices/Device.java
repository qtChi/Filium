package com.filium.model.devices;

import com.filium.model.network.NetworkInterface;
import com.filium.packet.Packet;

import java.util.Objects;
import java.util.UUID;

/**
 * Abstract base class for all network devices in Filium.
 * Holds identity, canvas position, and a single network interface.
 * Equality is based solely on the device UUID.
 */
public abstract class Device {

    private final String id;
    private String name;
    private final DeviceType deviceType;
    private final NetworkInterface networkInterface;
    private double x;
    private double y;

    /**
     * Constructs a Device with an auto-generated UUID.
     *
     * @param name       the user-assigned label for this device; must not be null
     * @param deviceType the type of device; must not be null
     * @throws IllegalArgumentException if name or deviceType is null
     */
    protected Device(String name, DeviceType deviceType) {
        if (name == null) {
            throw new IllegalArgumentException("Device name must not be null");
        }
        if (deviceType == null) {
            throw new IllegalArgumentException("DeviceType must not be null");
        }
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.deviceType = deviceType;
        this.networkInterface = new NetworkInterface();
        this.x = 0.0;
        this.y = 0.0;
    }

    /**
     * Returns the unique identifier for this device.
     *
     * @return UUID string
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the user-assigned name of this device.
     *
     * @return device name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the user-assigned name of this device.
     *
     * @param name the new name; must not be null
     * @throws IllegalArgumentException if name is null
     */
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Device name must not be null");
        }
        this.name = name;
    }

    /**
     * Returns the type of this device.
     *
     * @return DeviceType
     */
    public DeviceType getDeviceType() {
        return deviceType;
    }

    /**
     * Returns the network interface for this device.
     *
     * @return NetworkInterface
     */
    public NetworkInterface getNetworkInterface() {
        return networkInterface;
    }

    /**
     * Returns the canvas X position of this device.
     *
     * @return X coordinate in pixels
     */
    public double getX() {
        return x;
    }

    /**
     * Sets the canvas X position of this device.
     *
     * @param x X coordinate in pixels
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Returns the canvas Y position of this device.
     *
     * @return Y coordinate in pixels
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the canvas Y position of this device.
     *
     * @param y Y coordinate in pixels
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Handles an incoming packet. Each subclass processes packets differently
     * depending on its role in the network.
     *
     * @param packet the incoming packet; must not be null
     */
    public abstract void receivePacket(Packet packet);

    /**
     * Resets this device to its initial state, clearing any learned state
     * such as ARP caches, DNS records, or received packet lists.
     */
    public abstract void reset();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Device other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return deviceType + "{id=" + id + ", name=" + name + "}";
    }
}
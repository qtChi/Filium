package com.filium.model.network;

import com.filium.model.devices.Device;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a network cable connecting exactly two devices.
 * Cables are undirected — endpointA and endpointB have no implied direction.
 * Equality is based solely on the cable UUID.
 */
public class Cable {

    private final String id;
    private final Device endpointA;
    private final Device endpointB;

    /**
     * Constructs a Cable between two devices.
     *
     * @param endpointA the first device; must not be null
     * @param endpointB the second device; must not be null
     * @throws IllegalArgumentException if either endpoint is null or if
     *                                  both endpoints are the same device instance
     */
    public Cable(Device endpointA, Device endpointB) {
        if (endpointA == null) {
            throw new IllegalArgumentException("Endpoint A must not be null");
        }
        if (endpointB == null) {
            throw new IllegalArgumentException("Endpoint B must not be null");
        }
        if (endpointA == endpointB) {
            throw new IllegalArgumentException(
                "A cable cannot connect a device to itself");
        }
        this.id = UUID.randomUUID().toString();
        this.endpointA = endpointA;
        this.endpointB = endpointB;
    }

    /**
     * Returns the unique identifier for this cable.
     *
     * @return UUID string
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the first endpoint device.
     *
     * @return Device endpointA
     */
    public Device getEndpointA() {
        return endpointA;
    }

    /**
     * Returns the second endpoint device.
     *
     * @return Device endpointB
     */
    public Device getEndpointB() {
        return endpointB;
    }

    /**
     * Returns true if the given device is one of the two endpoints of this cable.
     *
     * @param device the device to check; must not be null
     * @return true if the device is endpointA or endpointB
     * @throws IllegalArgumentException if device is null
     */
    public boolean connects(Device device) {
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null");
        }
        return endpointA.equals(device) || endpointB.equals(device);
    }

    /**
     * Given one endpoint, returns the device at the other end of the cable.
     *
     * @param device one of the two endpoints; must not be null
     * @return the other endpoint Device
     * @throws IllegalArgumentException if device is null or not an endpoint of this cable
     */
    public Device getOtherEnd(Device device) {
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null");
        }
        if (endpointA.equals(device)) {
            return endpointB;
        }
        if (endpointB.equals(device)) {
            return endpointA;
        }
        throw new IllegalArgumentException(
            "Device '" + device.getName() + "' is not an endpoint of this cable");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Cable other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Cable{id=" + id +
               ", a=" + endpointA.getName() +
               ", b=" + endpointB.getName() + "}";
    }
}
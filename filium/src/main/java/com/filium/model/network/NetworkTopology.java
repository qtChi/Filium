package com.filium.model.network;

import com.filium.model.devices.Device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Holds the complete set of devices and cables that make up a network topology.
 * Acts as the central data store — devices and cables are added/removed here,
 * and the SimulationEngine reads from this topology when routing packets.
 */
public class NetworkTopology {

    private final List<Device> devices;
    private final List<Cable> cables;

    /**
     * Constructs an empty NetworkTopology.
     */
    public NetworkTopology() {
        this.devices = new ArrayList<>();
        this.cables = new ArrayList<>();
    }

    /**
     * Adds a device to the topology.
     *
     * @param device the device to add; must not be null
     * @throws IllegalArgumentException if device is null
     * @throws IllegalStateException    if a device with the same ID already exists
     */
    public void addDevice(Device device) {
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null");
        }
        for (Device existing : devices) {
            if (existing.getId().equals(device.getId())) {
                throw new IllegalStateException(
                    "A device with ID '" + device.getId() + "' already exists");
            }
        }
        devices.add(device);
    }

    /**
     * Removes a device and all cables connected to it from the topology.
     *
     * @param device the device to remove; must not be null
     * @return true if the device was found and removed, false if not present
     * @throws IllegalArgumentException if device is null
     */
    public boolean removeDevice(Device device) {
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null");
        }
        boolean removed = devices.remove(device);
        if (removed) {
            cables.removeIf(cable -> cable.connects(device));
        }
        return removed;
    }

    /**
     * Returns an unmodifiable view of all devices in the topology.
     *
     * @return unmodifiable list of Device
     */
    public List<Device> getDevices() {
        return Collections.unmodifiableList(devices);
    }

    /**
     * Finds a device by its unique ID.
     *
     * @param id the UUID string to search for
     * @return Optional containing the Device if found, empty otherwise
     */
    public Optional<Device> findDeviceById(String id) {
        if (id == null) return Optional.empty();
        return devices.stream()
            .filter(d -> d.getId().equals(id))
            .findFirst();
    }

    /**
     * Finds a device by its name. Returns the first match if multiple devices
     * share the same name.
     *
     * @param name the name to search for
     * @return Optional containing the Device if found, empty otherwise
     */
    public Optional<Device> findDeviceByName(String name) {
        if (name == null) return Optional.empty();
        return devices.stream()
            .filter(d -> d.getName().equals(name))
            .findFirst();
    }

    /**
     * Adds a cable to the topology. Both endpoints must already be present
     * in the topology.
     *
     * @param cable the cable to add; must not be null
     * @throws IllegalArgumentException if cable is null or if either endpoint
     *                                  is not registered in this topology
     */
    public void addCable(Cable cable) {
        if (cable == null) {
            throw new IllegalArgumentException("Cable must not be null");
        }
        if (!devices.contains(cable.getEndpointA())) {
            throw new IllegalArgumentException(
                "Endpoint A '" + cable.getEndpointA().getName()
                + "' is not in this topology");
        }
        if (!devices.contains(cable.getEndpointB())) {
            throw new IllegalArgumentException(
                "Endpoint B '" + cable.getEndpointB().getName()
                + "' is not in this topology");
        }
        cables.add(cable);
    }

    /**
     * Removes a cable from the topology.
     *
     * @param cable the cable to remove; must not be null
     * @return true if the cable was found and removed, false if not present
     * @throws IllegalArgumentException if cable is null
     */
    public boolean removeCable(Cable cable) {
        if (cable == null) {
            throw new IllegalArgumentException("Cable must not be null");
        }
        return cables.remove(cable);
    }

    /**
     * Returns an unmodifiable view of all cables in the topology.
     *
     * @return unmodifiable list of Cable
     */
    public List<Cable> getCables() {
        return Collections.unmodifiableList(cables);
    }

    /**
     * Returns all cables directly connected to the given device.
     *
     * @param device the device to query; must not be null
     * @return list of cables that have the given device as an endpoint
     * @throws IllegalArgumentException if device is null
     */
    public List<Cable> getCablesForDevice(Device device) {
        if (device == null) {
            throw new IllegalArgumentException("Device must not be null");
        }
        List<Cable> result = new ArrayList<>();
        for (Cable cable : cables) {
            if (cable.connects(device)) {
                result.add(cable);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Returns true if the two given devices are directly connected by a cable.
     *
     * @param a first device; must not be null
     * @param b second device; must not be null
     * @return true if a cable exists between a and b
     * @throws IllegalArgumentException if either device is null
     */
    public boolean areConnected(Device a, Device b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Devices must not be null");
        }
        for (Cable cable : cables) {
            if (cable.connects(a) && cable.connects(b)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clears all devices and cables from the topology.
     */
    public void clear() {
        devices.clear();
        cables.clear();
    }

    /**
     * Returns the number of devices in the topology.
     *
     * @return device count
     */
    public int deviceCount() {
        return devices.size();
    }

    /**
     * Returns the number of cables in the topology.
     *
     * @return cable count
     */
    public int cableCount() {
        return cables.size();
    }
}
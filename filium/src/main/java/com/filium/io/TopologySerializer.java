package com.filium.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.filium.model.devices.Device;
import com.filium.model.network.Cable;
import com.filium.model.network.NetworkTopology;

import java.io.File;
import java.io.IOException;

/**
 * Serializes a NetworkTopology to a JSON file.
 * Each device is written with its id, name, type, IP, MAC, and canvas position.
 * Each cable is written with its id and the IDs of its two endpoints.
 */
public class TopologySerializer {

    private final ObjectMapper mapper;

    /** Constructs a TopologySerializer with pretty-printing enabled. */
    public TopologySerializer() {
        this.mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Serializes the given topology to the specified file.
     *
     * @param topology the topology to serialize; must not be null
     * @param file     the target file; must not be null
     * @throws IllegalArgumentException if topology or file is null
     * @throws IOException              if writing fails
     */
    public void serialize(NetworkTopology topology, File file) throws IOException {
        if (topology == null) throw new IllegalArgumentException("Topology must not be null");
        if (file == null)     throw new IllegalArgumentException("File must not be null");

        ObjectNode root = mapper.createObjectNode();

        ArrayNode devicesNode = mapper.createArrayNode();
        for (Device device : topology.getDevices()) {
            ObjectNode d = mapper.createObjectNode();
            d.put("id",   device.getId());
            d.put("name", device.getName());
            d.put("type", device.getDeviceType().name());
            d.put("mac",  device.getNetworkInterface().getMacAddress());
            d.put("ip",   device.getNetworkInterface().getIpAddress() != null
                ? device.getNetworkInterface().getIpAddress().getAddress() : "");
            d.put("mask", device.getNetworkInterface().getSubnetMask());
            d.put("x",    device.getX());
            d.put("y",    device.getY());
            devicesNode.add(d);
        }
        root.set("devices", devicesNode);

        ArrayNode cablesNode = mapper.createArrayNode();
        for (Cable cable : topology.getCables()) {
            ObjectNode c = mapper.createObjectNode();
            c.put("id",          cable.getId());
            c.put("endpointAId", cable.getEndpointA().getId());
            c.put("endpointBId", cable.getEndpointB().getId());
            cablesNode.add(c);
        }
        root.set("cables", cablesNode);

        mapper.writeValue(file, root);
    }
}
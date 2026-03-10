package com.filium.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.filium.model.devices.*;
import com.filium.model.network.Cable;
import com.filium.model.network.IPAddress;
import com.filium.model.network.NetworkTopology;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Deserializes a NetworkTopology from a JSON file produced by TopologySerializer.
 */
public class TopologyDeserializer {

    private final ObjectMapper mapper;

    /** Constructs a TopologyDeserializer. */
    public TopologyDeserializer() {
        this.mapper = new ObjectMapper();
    }

    /**
     * Deserializes a NetworkTopology from the given JSON file.
     *
     * @param file the source file; must not be null and must exist
     * @return reconstructed NetworkTopology
     * @throws IllegalArgumentException if file is null
     * @throws IOException              if reading or parsing fails
     */
    public NetworkTopology deserialize(File file) throws IOException {
        if (file == null) throw new IllegalArgumentException("File must not be null");

        JsonNode root = mapper.readTree(file);
        NetworkTopology topology = new NetworkTopology();
        Map<String, Device> byId = new HashMap<>();

        JsonNode devicesNode = root.get("devices");
        if (devicesNode != null) {
            for (JsonNode d : devicesNode) {
                String typeName = d.get("type").asText();
                String name     = d.get("name").asText();
                DeviceType type = DeviceType.valueOf(typeName);

                Device device = createDevice(type, name);

                String ip   = d.get("ip").asText();
                String mask = d.get("mask").asText();
                if (!ip.isBlank()) {
                    device.getNetworkInterface().setIpAddress(new IPAddress(ip));
                }
                device.getNetworkInterface().setSubnetMask(mask);
                device.setX(d.get("x").asDouble());
                device.setY(d.get("y").asDouble());

                topology.addDevice(device);
                byId.put(d.get("id").asText(), device);
            }
        }

        JsonNode cablesNode = root.get("cables");
        if (cablesNode != null) {
            for (JsonNode c : cablesNode) {
                String aId = c.get("endpointAId").asText();
                String bId = c.get("endpointBId").asText();
                Device a   = byId.get(aId);
                Device b   = byId.get(bId);
                if (a != null && b != null) {
                    topology.addCable(new Cable(a, b));
                }
            }
        }

        return topology;
    }

    private Device createDevice(DeviceType type, String name) {
        return switch (type) {
            case PC          -> new PC(name);
            case ROUTER      -> new Router(name);
            case SWITCH      -> new Switch(name);
            case DNS_SERVER  -> new DNSServer(name);
            case DHCP_SERVER -> new DHCPServer(name);
            case FIREWALL    -> new Firewall(name);
        };
    }
}
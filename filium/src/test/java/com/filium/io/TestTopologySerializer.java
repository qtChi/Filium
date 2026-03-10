package com.filium.io;

import com.filium.model.devices.PC;
import com.filium.model.devices.Router;
import com.filium.model.network.Cable;
import com.filium.model.network.IPAddress;
import com.filium.model.network.NetworkTopology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TestTopologySerializer {

    @TempDir File tempDir;

    private TopologySerializer serializer;
    private NetworkTopology topology;
    private PC pcA;
    private Router router;

    @BeforeEach void setUp() {
        serializer = new TopologySerializer();
        topology   = new NetworkTopology();
        pcA        = new PC("PC-A");
        pcA.getNetworkInterface().setIpAddress(new IPAddress("192.168.1.10"));
        pcA.setX(100.0); pcA.setY(200.0);
        router     = new Router("Router-1");
        router.getNetworkInterface().setIpAddress(new IPAddress("192.168.1.1"));
        topology.addDevice(pcA); topology.addDevice(router);
        topology.addCable(new Cable(pcA, router));
    }

    // constructor validation
    @Test void serialize_nullTopology_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> serializer.serialize(null, new File(tempDir, "out.json")));
    }
    @Test void serialize_nullFile_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> serializer.serialize(topology, null));
    }

    // file creation
    @Test void serialize_createsFile() throws IOException {
        File out = new File(tempDir, "topology.json");
        serializer.serialize(topology, out);
        assertTrue(out.exists());
    }
    @Test void serialize_fileIsNonEmpty() throws IOException {
        File out = new File(tempDir, "topology.json");
        serializer.serialize(topology, out);
        assertTrue(out.length() > 0);
    }

    // round-trip via deserializer
    @Test void serialize_deserialize_roundTrip_deviceCount() throws IOException {
        File out = new File(tempDir, "topology.json");
        serializer.serialize(topology, out);
        TopologyDeserializer deserializer = new TopologyDeserializer();
        NetworkTopology loaded = deserializer.deserialize(out);
        assertEquals(2, loaded.deviceCount());
    }
    @Test void serialize_deserialize_roundTrip_cableCount() throws IOException {
        File out = new File(tempDir, "topology.json");
        serializer.serialize(topology, out);
        TopologyDeserializer deserializer = new TopologyDeserializer();
        NetworkTopology loaded = deserializer.deserialize(out);
        assertEquals(1, loaded.cableCount());
    }
    @Test void serialize_deserialize_roundTrip_deviceName() throws IOException {
        File out = new File(tempDir, "topology.json");
        serializer.serialize(topology, out);
        TopologyDeserializer deserializer = new TopologyDeserializer();
        NetworkTopology loaded = deserializer.deserialize(out);
        assertTrue(loaded.findDeviceByName("PC-A").isPresent());
    }
    @Test void serialize_deserialize_roundTrip_ipAddress() throws IOException {
        File out = new File(tempDir, "topology.json");
        serializer.serialize(topology, out);
        TopologyDeserializer deserializer = new TopologyDeserializer();
        NetworkTopology loaded = deserializer.deserialize(out);
        IPAddress ip = loaded.findDeviceByName("PC-A")
            .orElseThrow().getNetworkInterface().getIpAddress();
        assertNotNull(ip);
        assertEquals("192.168.1.10", ip.getAddress());
    }
    @Test void serialize_deserialize_roundTrip_canvasPosition() throws IOException {
        File out = new File(tempDir, "topology.json");
        serializer.serialize(topology, out);
        TopologyDeserializer deserializer = new TopologyDeserializer();
        NetworkTopology loaded = deserializer.deserialize(out);
        assertEquals(100.0, loaded.findDeviceByName("PC-A").orElseThrow().getX());
        assertEquals(200.0, loaded.findDeviceByName("PC-A").orElseThrow().getY());
    }

    // empty topology
    @Test void serialize_emptyTopology_createsValidFile() throws IOException {
        File out = new File(tempDir, "empty.json");
        serializer.serialize(new NetworkTopology(), out);
        assertTrue(out.exists());
        TopologyDeserializer deserializer = new TopologyDeserializer();
        NetworkTopology loaded = deserializer.deserialize(out);
        assertEquals(0, loaded.deviceCount());
        assertEquals(0, loaded.cableCount());
    }

    // device with no IP (empty string in JSON)
    @Test void serialize_deviceWithNoIP_roundTripsCorrectly() throws IOException {
        PC noIP = new PC("NoIP-PC");
        NetworkTopology t = new NetworkTopology(); t.addDevice(noIP);
        File out = new File(tempDir, "noip.json");
        serializer.serialize(t, out);
        TopologyDeserializer deserializer = new TopologyDeserializer();
        NetworkTopology loaded = deserializer.deserialize(out);
        assertNull(loaded.findDeviceByName("NoIP-PC")
            .orElseThrow().getNetworkInterface().getIpAddress());
    }
}
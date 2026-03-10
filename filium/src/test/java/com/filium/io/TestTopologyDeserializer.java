package com.filium.io;

import com.filium.model.network.NetworkTopology;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class TestTopologyDeserializer {

    @TempDir File tempDir;

    private TopologyDeserializer deserializer;

    @BeforeEach void setUp() { deserializer = new TopologyDeserializer(); }

    private File writeJson(String content) throws IOException {
        File f = new File(tempDir, "topology.json");
        try (FileWriter w = new FileWriter(f)) { w.write(content); }
        return f;
    }

    @Test void deserialize_nullFile_throws() {
        assertThrows(IllegalArgumentException.class,
            () -> deserializer.deserialize(null));
    }

    @Test void deserialize_emptyDevicesArray_returnsEmptyTopology() throws IOException {
        File f = writeJson("{\"devices\":[],\"cables\":[]}");
        NetworkTopology t = deserializer.deserialize(f);
        assertEquals(0, t.deviceCount());
        assertEquals(0, t.cableCount());
    }

    @Test void deserialize_allDeviceTypes_createsCorrectly() throws IOException {
        String json = """
            {"devices":[
              {"id":"1","name":"PC1","type":"PC","mac":"AA:BB:CC:DD:EE:FF",
               "ip":"10.0.0.1","mask":"255.255.255.0","x":0,"y":0},
              {"id":"2","name":"R1","type":"ROUTER","mac":"BB:BB:BB:BB:BB:BB",
               "ip":"10.0.0.254","mask":"255.255.255.0","x":10,"y":10},
              {"id":"3","name":"SW1","type":"SWITCH","mac":"CC:CC:CC:CC:CC:CC",
               "ip":"","mask":"255.255.255.0","x":0,"y":0},
              {"id":"4","name":"DNS1","type":"DNS_SERVER","mac":"DD:DD:DD:DD:DD:DD",
               "ip":"10.0.0.53","mask":"255.255.255.0","x":0,"y":0},
              {"id":"5","name":"DHCP1","type":"DHCP_SERVER","mac":"EE:EE:EE:EE:EE:EE",
               "ip":"10.0.0.67","mask":"255.255.255.0","x":0,"y":0},
              {"id":"6","name":"FW1","type":"FIREWALL","mac":"FF:FF:FF:FF:FF:FF",
               "ip":"10.0.0.254","mask":"255.255.255.0","x":0,"y":0}
            ],"cables":[]}""";
        NetworkTopology t = deserializer.deserialize(writeJson(json));
        assertEquals(6, t.deviceCount());
    }

    @Test void deserialize_withCables_createsCables() throws IOException {
        String json = """
            {"devices":[
              {"id":"1","name":"PC1","type":"PC","mac":"AA:BB:CC:DD:EE:FF",
               "ip":"10.0.0.1","mask":"255.255.255.0","x":0,"y":0},
              {"id":"2","name":"PC2","type":"PC","mac":"BB:BB:BB:BB:BB:BB",
               "ip":"10.0.0.2","mask":"255.255.255.0","x":10,"y":10}
            ],"cables":[
              {"id":"c1","endpointAId":"1","endpointBId":"2"}
            ]}""";
        NetworkTopology t = deserializer.deserialize(writeJson(json));
        assertEquals(1, t.cableCount());
    }

    @Test void deserialize_cableWithUnknownEndpoint_skipsGracefully() throws IOException {
        String json = """
            {"devices":[
              {"id":"1","name":"PC1","type":"PC","mac":"AA:BB:CC:DD:EE:FF",
               "ip":"10.0.0.1","mask":"255.255.255.0","x":0,"y":0}
            ],"cables":[
              {"id":"c1","endpointAId":"1","endpointBId":"UNKNOWN"}
            ]}""";
        NetworkTopology t = deserializer.deserialize(writeJson(json));
        assertEquals(0, t.cableCount()); // skipped silently
    }

    @Test void deserialize_nullDevicesNode_returnsEmptyTopology() throws IOException {
        File f = writeJson("{}");
        NetworkTopology t = deserializer.deserialize(f);
        assertEquals(0, t.deviceCount());
        assertEquals(0, t.cableCount());
    }
}
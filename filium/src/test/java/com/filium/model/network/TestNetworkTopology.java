package com.filium.model.network;

import com.filium.model.devices.PC;
import com.filium.model.devices.Router;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NetworkTopology.
 *
 * Input partitions covered:
 *  - Construction: empty devices, empty cables
 *  - addDevice(): valid device, null device throws, duplicate ID throws
 *  - removeDevice(): existing device returns true, also removes its cables,
 *    non-existent device returns false, null throws
 *  - getDevices(): unmodifiable, reflects adds/removes
 *  - findDeviceById(): found, not found, null id
 *  - findDeviceByName(): found, not found (first match), null name
 *  - addCable(): valid cable, null throws, endpoint A not in topology throws,
 *    endpoint B not in topology throws
 *  - removeCable(): existing cable returns true,
 *    non-existent cable returns false, null throws
 *  - getCables(): unmodifiable
 *  - getCablesForDevice(): device with cables, device with no cables,
 *    null throws
 *  - areConnected(): connected pair returns true, unconnected pair returns false,
 *    null a throws, null b throws
 *  - clear(): removes all devices and cables
 *  - deviceCount() / cableCount(): correct counts after mutations
 */
class TestNetworkTopology {

    private NetworkTopology topology;
    private PC pcA;
    private PC pcB;
    private PC pcC;
    private Router router;

    @BeforeEach
    void setUp() {
        topology = new NetworkTopology();
        pcA    = new PC("PC-A");
        pcB    = new PC("PC-B");
        pcC    = new PC("PC-C");
        router = new Router("Router-1");
    }

    // ─────────────────────────────────────────────────────────────────
    // Construction
    // ─────────────────────────────────────────────────────────────────

    @Test
    void constructor_devicesEmptyInitially() {
        assertTrue(topology.getDevices().isEmpty());
    }

    @Test
    void constructor_cablesEmptyInitially() {
        assertTrue(topology.getCables().isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────
    // addDevice()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void addDevice_validDevice_increasesDeviceCount() {
        topology.addDevice(pcA);
        assertEquals(1, topology.deviceCount());
    }

    @Test
    void addDevice_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> topology.addDevice(null));
    }

    @Test
    void addDevice_duplicateId_throwsIllegalStateException() {
        topology.addDevice(pcA);
        assertThrows(IllegalStateException.class,
            () -> topology.addDevice(pcA));
    }

    // ─────────────────────────────────────────────────────────────────
    // removeDevice()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void removeDevice_existingDevice_returnsTrue() {
        topology.addDevice(pcA);
        assertTrue(topology.removeDevice(pcA));
    }

    @Test
    void removeDevice_existingDevice_decreasesCount() {
        topology.addDevice(pcA);
        topology.removeDevice(pcA);
        assertEquals(0, topology.deviceCount());
    }

    @Test
    void removeDevice_alsoRemovesConnectedCables() {
        topology.addDevice(pcA);
        topology.addDevice(pcB);
        Cable cable = new Cable(pcA, pcB);
        topology.addCable(cable);
        topology.removeDevice(pcA);
        assertEquals(0, topology.cableCount());
    }

    @Test
    void removeDevice_nonExistentDevice_returnsFalse() {
        assertFalse(topology.removeDevice(pcA));
    }

    @Test
    void removeDevice_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> topology.removeDevice(null));
    }

    // ─────────────────────────────────────────────────────────────────
    // getDevices()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getDevices_returnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class,
            () -> topology.getDevices().add(pcA));
    }

    @Test
    void getDevices_reflectsAddedDevices() {
        topology.addDevice(pcA);
        topology.addDevice(pcB);
        List<PC> expected = List.of(pcA, pcB);
        assertTrue(topology.getDevices().containsAll(expected));
    }

    // ─────────────────────────────────────────────────────────────────
    // findDeviceById()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void findDeviceById_knownId_returnsDevice() {
        topology.addDevice(pcA);
        Optional<?> found = topology.findDeviceById(pcA.getId());
        assertTrue(found.isPresent());
        assertEquals(pcA, found.get());
    }

    @Test
    void findDeviceById_unknownId_returnsEmpty() {
        assertTrue(topology.findDeviceById("no-such-id").isEmpty());
    }

    @Test
    void findDeviceById_nullId_returnsEmpty() {
        assertTrue(topology.findDeviceById(null).isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────
    // findDeviceByName()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void findDeviceByName_knownName_returnsDevice() {
        topology.addDevice(pcA);
        Optional<?> found = topology.findDeviceByName("PC-A");
        assertTrue(found.isPresent());
        assertEquals(pcA, found.get());
    }

    @Test
    void findDeviceByName_unknownName_returnsEmpty() {
        assertTrue(topology.findDeviceByName("Ghost").isEmpty());
    }

    @Test
    void findDeviceByName_nullName_returnsEmpty() {
        assertTrue(topology.findDeviceByName(null).isEmpty());
    }

    @Test
    void findDeviceByName_duplicateNames_returnsFirstMatch() {
        PC pcA2 = new PC("PC-A");
        topology.addDevice(pcA);
        topology.addDevice(pcA2);
        assertEquals(pcA, topology.findDeviceByName("PC-A").get());
    }

    // ─────────────────────────────────────────────────────────────────
    // addCable()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void addCable_validCable_increasesCableCount() {
        topology.addDevice(pcA);
        topology.addDevice(pcB);
        topology.addCable(new Cable(pcA, pcB));
        assertEquals(1, topology.cableCount());
    }

    @Test
    void addCable_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> topology.addCable(null));
    }

    @Test
    void addCable_endpointANotInTopology_throwsIllegalArgumentException() {
        topology.addDevice(pcB);
        assertThrows(IllegalArgumentException.class,
            () -> topology.addCable(new Cable(pcA, pcB)));
    }

    @Test
    void addCable_endpointBNotInTopology_throwsIllegalArgumentException() {
        topology.addDevice(pcA);
        assertThrows(IllegalArgumentException.class,
            () -> topology.addCable(new Cable(pcA, pcB)));
    }

    // ─────────────────────────────────────────────────────────────────
    // removeCable()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void removeCable_existingCable_returnsTrue() {
        topology.addDevice(pcA);
        topology.addDevice(pcB);
        Cable cable = new Cable(pcA, pcB);
        topology.addCable(cable);
        assertTrue(topology.removeCable(cable));
    }

    @Test
    void removeCable_existingCable_decreasesCount() {
        topology.addDevice(pcA);
        topology.addDevice(pcB);
        Cable cable = new Cable(pcA, pcB);
        topology.addCable(cable);
        topology.removeCable(cable);
        assertEquals(0, topology.cableCount());
    }

    @Test
    void removeCable_nonExistentCable_returnsFalse() {
        topology.addDevice(pcA);
        topology.addDevice(pcB);
        Cable cable = new Cable(pcA, pcB);
        assertFalse(topology.removeCable(cable));
    }

    @Test
    void removeCable_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> topology.removeCable(null));
    }

    // ─────────────────────────────────────────────────────────────────
    // getCables()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getCables_returnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class,
            () -> topology.getCables().clear());
    }

    // ─────────────────────────────────────────────────────────────────
    // getCablesForDevice()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getCablesForDevice_deviceWithCables_returnsCorrectCables() {
        topology.addDevice(pcA);
        topology.addDevice(pcB);
        topology.addDevice(pcC);
        Cable ab = new Cable(pcA, pcB);
        Cable ac = new Cable(pcA, pcC);
        topology.addCable(ab);
        topology.addCable(ac);
        List<Cable> cablesForA = topology.getCablesForDevice(pcA);
        assertEquals(2, cablesForA.size());
        assertTrue(cablesForA.contains(ab));
        assertTrue(cablesForA.contains(ac));
    }

    @Test
    void getCablesForDevice_deviceWithNoCables_returnsEmpty() {
        topology.addDevice(pcA);
        assertTrue(topology.getCablesForDevice(pcA).isEmpty());
    }

    @Test
    void getCablesForDevice_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> topology.getCablesForDevice(null));
    }

    @Test
    void getCablesForDevice_returnsUnmodifiableList() {
        topology.addDevice(pcA);
        assertThrows(UnsupportedOperationException.class,
            () -> topology.getCablesForDevice(pcA).add(new Cable(pcA, pcB)));
    }

    // ─────────────────────────────────────────────────────────────────
    // areConnected()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void areConnected_directlyConnectedPair_returnsTrue() {
        topology.addDevice(pcA);
        topology.addDevice(pcB);
        topology.addCable(new Cable(pcA, pcB));
        assertTrue(topology.areConnected(pcA, pcB));
    }

    @Test
    void areConnected_symmetrical_reverseOrderAlsoReturnsTrue() {
        topology.addDevice(pcA);
        topology.addDevice(pcB);
        topology.addCable(new Cable(pcA, pcB));
        assertTrue(topology.areConnected(pcB, pcA));
    }

    @Test
    void areConnected_notConnected_returnsFalse() {
        topology.addDevice(pcA);
        topology.addDevice(pcB);
        assertFalse(topology.areConnected(pcA, pcB));
    }

    @Test
    void areConnected_nullA_throwsIllegalArgumentException() {
        topology.addDevice(pcB);
        assertThrows(IllegalArgumentException.class,
            () -> topology.areConnected(null, pcB));
    }

    @Test
    void areConnected_nullB_throwsIllegalArgumentException() {
        topology.addDevice(pcA);
        assertThrows(IllegalArgumentException.class,
            () -> topology.areConnected(pcA, null));
    }

    // ─────────────────────────────────────────────────────────────────
    // clear()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void clear_removesAllDevices() {
        topology.addDevice(pcA);
        topology.addDevice(pcB);
        topology.clear();
        assertEquals(0, topology.deviceCount());
    }

    @Test
    void clear_removesAllCables() {
        topology.addDevice(pcA);
        topology.addDevice(pcB);
        topology.addCable(new Cable(pcA, pcB));
        topology.clear();
        assertEquals(0, topology.cableCount());
    }

    @Test
    void clear_onEmptyTopology_doesNotThrow() {
        assertDoesNotThrow(() -> topology.clear());
    }

    // ─────────────────────────────────────────────────────────────────
    // deviceCount() / cableCount()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void deviceCount_emptyTopology_returnsZero() {
        assertEquals(0, topology.deviceCount());
    }

    @Test
    void deviceCount_afterAdding_returnsCorrectCount() {
        topology.addDevice(pcA);
        topology.addDevice(pcB);
        assertEquals(2, topology.deviceCount());
    }

    @Test
    void cableCount_emptyTopology_returnsZero() {
        assertEquals(0, topology.cableCount());
    }

    @Test
    void cableCount_afterAdding_returnsCorrectCount() {
        topology.addDevice(pcA);
        topology.addDevice(pcB);
        topology.addDevice(pcC);
        topology.addCable(new Cable(pcA, pcB));
        topology.addCable(new Cable(pcB, pcC));
        assertEquals(2, topology.cableCount());
    }
}
package com.filium.model.network;

import com.filium.model.devices.PC;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Cable.
 *
 * Input partitions covered:
 *  - Construction: valid pair, null endpointA, null endpointB,
 *    same device for both endpoints (self-loop)
 *  - getId(): non-null, unique per instance
 *  - getEndpointA() / getEndpointB(): return correct devices
 *  - connects(): endpointA matches, endpointB matches,
 *    unrelated device returns false, null throws
 *  - getOtherEnd(): given A returns B, given B returns A,
 *    unrelated device throws, null throws
 *  - equals()/hashCode(): same instance, different cables, null, different type
 *  - toString(): contains endpoint names
 */
class TestCable {

    private PC deviceA;
    private PC deviceB;
    private PC deviceC;

    @BeforeEach
    void setUp() {
        deviceA = new PC("PC-A");
        deviceB = new PC("PC-B");
        deviceC = new PC("PC-C");
    }

    // ─────────────────────────────────────────────────────────────────
    // Construction
    // ─────────────────────────────────────────────────────────────────

    @Test
    void constructor_validPair_createsSuccessfully() {
        assertDoesNotThrow(() -> new Cable(deviceA, deviceB));
    }

    @Test
    void constructor_nullEndpointA_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new Cable(null, deviceB));
    }

    @Test
    void constructor_nullEndpointB_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new Cable(deviceA, null));
    }

    @Test
    void constructor_sameDeviceBothEnds_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new Cable(deviceA, deviceA));
    }

    // ─────────────────────────────────────────────────────────────────
    // getId()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getId_returnsNonNull() {
        assertNotNull(new Cable(deviceA, deviceB).getId());
    }

    @Test
    void getId_uniquePerInstance() {
        Cable c1 = new Cable(deviceA, deviceB);
        Cable c2 = new Cable(deviceA, deviceB);
        assertNotEquals(c1.getId(), c2.getId());
    }

    // ─────────────────────────────────────────────────────────────────
    // getEndpointA() / getEndpointB()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getEndpointA_returnsCorrectDevice() {
        Cable cable = new Cable(deviceA, deviceB);
        assertEquals(deviceA, cable.getEndpointA());
    }

    @Test
    void getEndpointB_returnsCorrectDevice() {
        Cable cable = new Cable(deviceA, deviceB);
        assertEquals(deviceB, cable.getEndpointB());
    }

    // ─────────────────────────────────────────────────────────────────
    // connects()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void connects_endpointA_returnsTrue() {
        Cable cable = new Cable(deviceA, deviceB);
        assertTrue(cable.connects(deviceA));
    }

    @Test
    void connects_endpointB_returnsTrue() {
        Cable cable = new Cable(deviceA, deviceB);
        assertTrue(cable.connects(deviceB));
    }

    @Test
    void connects_unrelatedDevice_returnsFalse() {
        Cable cable = new Cable(deviceA, deviceB);
        assertFalse(cable.connects(deviceC));
    }

    @Test
    void connects_null_throwsIllegalArgumentException() {
        Cable cable = new Cable(deviceA, deviceB);
        assertThrows(IllegalArgumentException.class,
            () -> cable.connects(null));
    }

    // ─────────────────────────────────────────────────────────────────
    // getOtherEnd()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getOtherEnd_givenA_returnsB() {
        Cable cable = new Cable(deviceA, deviceB);
        assertEquals(deviceB, cable.getOtherEnd(deviceA));
    }

    @Test
    void getOtherEnd_givenB_returnsA() {
        Cable cable = new Cable(deviceA, deviceB);
        assertEquals(deviceA, cable.getOtherEnd(deviceB));
    }

    @Test
    void getOtherEnd_unrelatedDevice_throwsIllegalArgumentException() {
        Cable cable = new Cable(deviceA, deviceB);
        assertThrows(IllegalArgumentException.class,
            () -> cable.getOtherEnd(deviceC));
    }

    @Test
    void getOtherEnd_null_throwsIllegalArgumentException() {
        Cable cable = new Cable(deviceA, deviceB);
        assertThrows(IllegalArgumentException.class,
            () -> cable.getOtherEnd(null));
    }

    // ─────────────────────────────────────────────────────────────────
    // equals() and hashCode()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void equals_sameInstance_returnsTrue() {
        Cable cable = new Cable(deviceA, deviceB);
        assertEquals(cable, cable);
    }

    @Test
    void equals_differentCables_returnsFalse() {
        Cable c1 = new Cable(deviceA, deviceB);
        Cable c2 = new Cable(deviceA, deviceB);
        assertNotEquals(c1, c2);
    }

    @Test
    void equals_null_returnsFalse() {
        assertNotEquals(null, new Cable(deviceA, deviceB));
    }

    @Test
    void equals_differentType_returnsFalse() {
        assertNotEquals("string", new Cable(deviceA, deviceB));
    }

    @Test
    void hashCode_sameInstance_consistent() {
        Cable cable = new Cable(deviceA, deviceB);
        assertEquals(cable.hashCode(), cable.hashCode());
    }

    @Test
    void hashCode_differentCables_differentHashCode() {
        Cable c1 = new Cable(deviceA, deviceB);
        Cable c2 = new Cable(deviceA, deviceB);
        assertNotEquals(c1.hashCode(), c2.hashCode());
    }

    // ─────────────────────────────────────────────────────────────────
    // toString()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void toString_containsEndpointAName() {
        Cable cable = new Cable(deviceA, deviceB);
        assertTrue(cable.toString().contains("PC-A"));
    }

    @Test
    void toString_containsEndpointBName() {
        Cable cable = new Cable(deviceA, deviceB);
        assertTrue(cable.toString().contains("PC-B"));
    }
}
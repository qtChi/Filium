package com.filium.model.devices;

import com.filium.packet.Packet;
import com.filium.packet.PacketHeader;
import com.filium.packet.PacketType;
import com.filium.model.network.IPAddress;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Device abstract base class, exercised via PC (the simplest subclass).
 *
 * Input partitions covered:
 *  - Construction: valid name+type, null name, null type
 *  - getId(): non-null, UUID format, unique per instance
 *  - getName() / setName(): get set value, set null throws
 *  - getDeviceType(): returns correct type
 *  - getNetworkInterface(): non-null
 *  - getX/setX/getY/setY(): default 0.0, set positive, set negative
 *  - equals()/hashCode(): same instance, different instances (different id),
 *    null, different type
 *  - toString(): contains device type and name
 */
class TestDevice {

    private static final String SRC_MAC = "AA:BB:CC:DD:EE:FF";
    private static final String DST_MAC = "11:22:33:44:55:66";

    private PC newPC(String name) {
        return new PC(name);
    }

    private Packet samplePacket() {
        PacketHeader h = new PacketHeader(
            new IPAddress("192.168.1.1"), new IPAddress("192.168.1.2"),
            SRC_MAC, DST_MAC, 64);
        return new Packet(PacketType.ICMP_ECHO_REQUEST, h, "");
    }

    // ─────────────────────────────────────────────────────────────────
    // Construction
    // ─────────────────────────────────────────────────────────────────

    @Test
    void constructor_validArgs_createsSuccessfully() {
        assertDoesNotThrow(() -> newPC("PC-1"));
    }

    @Test
    void constructor_nullName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new PC(null));
    }

    // ─────────────────────────────────────────────────────────────────
    // getId()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getId_returnsNonNull() {
        assertNotNull(newPC("PC-1").getId());
    }

    @Test
    void getId_isUUIDFormat() {
        String id = newPC("PC-1").getId();
        assertTrue(id.matches(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }

    @Test
    void getId_uniquePerInstance() {
        assertNotEquals(newPC("PC-1").getId(), newPC("PC-2").getId());
    }

    // ─────────────────────────────────────────────────────────────────
    // getName() / setName()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getName_returnsConstructorValue() {
        assertEquals("MyPC", newPC("MyPC").getName());
    }

    @Test
    void setName_validName_updatesName() {
        PC pc = newPC("Old");
        pc.setName("New");
        assertEquals("New", pc.getName());
    }

    @Test
    void setName_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> newPC("PC").setName(null));
    }

    // ─────────────────────────────────────────────────────────────────
    // getDeviceType()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getDeviceType_returnsCorrectType() {
        assertEquals(DeviceType.PC, newPC("PC-1").getDeviceType());
    }

    // ─────────────────────────────────────────────────────────────────
    // getNetworkInterface()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getNetworkInterface_returnsNonNull() {
        assertNotNull(newPC("PC-1").getNetworkInterface());
    }

    // ─────────────────────────────────────────────────────────────────
    // Canvas position
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getX_defaultIsZero() {
        assertEquals(0.0, newPC("PC-1").getX());
    }

    @Test
    void getY_defaultIsZero() {
        assertEquals(0.0, newPC("PC-1").getY());
    }

    @Test
    void setX_positiveValue_storesCorrectly() {
        PC pc = newPC("PC-1");
        pc.setX(150.5);
        assertEquals(150.5, pc.getX());
    }

    @Test
    void setX_negativeValue_storesCorrectly() {
        PC pc = newPC("PC-1");
        pc.setX(-10.0);
        assertEquals(-10.0, pc.getX());
    }

    @Test
    void setY_positiveValue_storesCorrectly() {
        PC pc = newPC("PC-1");
        pc.setY(200.0);
        assertEquals(200.0, pc.getY());
    }

    @Test
    void setY_negativeValue_storesCorrectly() {
        PC pc = newPC("PC-1");
        pc.setY(-5.0);
        assertEquals(-5.0, pc.getY());
    }

    // ─────────────────────────────────────────────────────────────────
    // equals() and hashCode()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void equals_sameInstance_returnsTrue() {
        PC pc = newPC("PC-1");
        assertEquals(pc, pc);
    }

    @Test
    void equals_differentInstances_returnsFalse() {
        assertNotEquals(newPC("PC-1"), newPC("PC-1"));
    }

    @Test
    void equals_null_returnsFalse() {
        assertNotEquals(null, newPC("PC-1"));
    }

    @Test
    void equals_differentType_returnsFalse() {
        assertNotEquals("not a device", newPC("PC-1"));
    }

    @Test
    void hashCode_sameInstance_consistent() {
        PC pc = newPC("PC-1");
        assertEquals(pc.hashCode(), pc.hashCode());
    }

    @Test
    void hashCode_differentInstances_different() {
        assertNotEquals(newPC("PC-1").hashCode(), newPC("PC-1").hashCode());
    }

    // ─────────────────────────────────────────────────────────────────
    // toString()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void toString_containsDeviceType() {
        assertTrue(newPC("PC-1").toString().contains("PC"));
    }

    @Test
    void toString_containsName() {
        assertTrue(newPC("MyDevice").toString().contains("MyDevice"));
    }
}
package com.filium.model.devices;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DeviceType enum.
 *
 * Input partitions covered:
 *  - All 6 enum values exist with correct displayName and iconPath
 *  - getDisplayName() / getIconPath() return non-null, non-blank values
 *  - fromDisplayName(): each valid display name, null input, unrecognised string
 *  - values() count matches expected
 */
class TestDeviceType {

    // ─────────────────────────────────────────────────────────────────
    // Enum values exist
    // ─────────────────────────────────────────────────────────────────

    @Test
    void values_containsAllSixDeviceTypes() {
        assertEquals(6, DeviceType.values().length);
    }

    @Test
    void pc_exists() {
        assertNotNull(DeviceType.PC);
    }

    @Test
    void router_exists() {
        assertNotNull(DeviceType.ROUTER);
    }

    @Test
    void switch_exists() {
        assertNotNull(DeviceType.SWITCH);
    }

    @Test
    void dnsServer_exists() {
        assertNotNull(DeviceType.DNS_SERVER);
    }

    @Test
    void dhcpServer_exists() {
        assertNotNull(DeviceType.DHCP_SERVER);
    }

    @Test
    void firewall_exists() {
        assertNotNull(DeviceType.FIREWALL);
    }

    // ─────────────────────────────────────────────────────────────────
    // getDisplayName()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getDisplayName_pc_returnsCorrectName() {
        assertEquals("PC", DeviceType.PC.getDisplayName());
    }

    @Test
    void getDisplayName_router_returnsCorrectName() {
        assertEquals("Router", DeviceType.ROUTER.getDisplayName());
    }

    @Test
    void getDisplayName_switch_returnsCorrectName() {
        assertEquals("Switch", DeviceType.SWITCH.getDisplayName());
    }

    @Test
    void getDisplayName_dnsServer_returnsCorrectName() {
        assertEquals("DNS Server", DeviceType.DNS_SERVER.getDisplayName());
    }

    @Test
    void getDisplayName_dhcpServer_returnsCorrectName() {
        assertEquals("DHCP Server", DeviceType.DHCP_SERVER.getDisplayName());
    }

    @Test
    void getDisplayName_firewall_returnsCorrectName() {
        assertEquals("Firewall", DeviceType.FIREWALL.getDisplayName());
    }

    @Test
    void getDisplayName_allValues_nonNullAndNonBlank() {
        for (DeviceType type : DeviceType.values()) {
            assertNotNull(type.getDisplayName(),
                type.name() + " should have a non-null displayName");
            assertFalse(type.getDisplayName().isBlank(),
                type.name() + " should have a non-blank displayName");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // getIconPath()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getIconPath_pc_returnsCorrectPath() {
        assertEquals("icons/pc.png", DeviceType.PC.getIconPath());
    }

    @Test
    void getIconPath_router_returnsCorrectPath() {
        assertEquals("icons/router.png", DeviceType.ROUTER.getIconPath());
    }

    @Test
    void getIconPath_switch_returnsCorrectPath() {
        assertEquals("icons/switch.png", DeviceType.SWITCH.getIconPath());
    }

    @Test
    void getIconPath_dnsServer_returnsCorrectPath() {
        assertEquals("icons/dns.png", DeviceType.DNS_SERVER.getIconPath());
    }

    @Test
    void getIconPath_dhcpServer_returnsCorrectPath() {
        assertEquals("icons/dhcp.png", DeviceType.DHCP_SERVER.getIconPath());
    }

    @Test
    void getIconPath_firewall_returnsCorrectPath() {
        assertEquals("icons/firewall.png", DeviceType.FIREWALL.getIconPath());
    }

    @Test
    void getIconPath_allValues_nonNullAndNonBlank() {
        for (DeviceType type : DeviceType.values()) {
            assertNotNull(type.getIconPath(),
                type.name() + " should have a non-null iconPath");
            assertFalse(type.getIconPath().isBlank(),
                type.name() + " should have a non-blank iconPath");
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // fromDisplayName()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void fromDisplayName_pc_returnsPC() {
        assertEquals(DeviceType.PC, DeviceType.fromDisplayName("PC"));
    }

    @Test
    void fromDisplayName_router_returnsRouter() {
        assertEquals(DeviceType.ROUTER, DeviceType.fromDisplayName("Router"));
    }

    @Test
    void fromDisplayName_switch_returnsSwitch() {
        assertEquals(DeviceType.SWITCH, DeviceType.fromDisplayName("Switch"));
    }

    @Test
    void fromDisplayName_dnsServer_returnsDNSServer() {
        assertEquals(DeviceType.DNS_SERVER, DeviceType.fromDisplayName("DNS Server"));
    }

    @Test
    void fromDisplayName_dhcpServer_returnsDHCPServer() {
        assertEquals(DeviceType.DHCP_SERVER, DeviceType.fromDisplayName("DHCP Server"));
    }

    @Test
    void fromDisplayName_firewall_returnsFirewall() {
        assertEquals(DeviceType.FIREWALL, DeviceType.fromDisplayName("Firewall"));
    }

    @Test
    void fromDisplayName_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> DeviceType.fromDisplayName(null));
    }

    @Test
    void fromDisplayName_unrecognisedName_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> DeviceType.fromDisplayName("Unknown Device"));
    }

    @Test
    void fromDisplayName_wrongCase_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> DeviceType.fromDisplayName("pc"));
    }

    @Test
    void fromDisplayName_emptyString_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> DeviceType.fromDisplayName(""));
    }
}
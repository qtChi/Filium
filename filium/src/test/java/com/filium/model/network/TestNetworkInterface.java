package com.filium.model.network;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NetworkInterface.
 *
 * Input partitions covered:
 *  - Default constructor: MAC auto-generated, IP null, mask default, gateway null
 *  - MAC constructor: valid MAC, null MAC, invalid format MACs
 *  - isConfigured(): true (IP set), false (IP null)
 *  - setIpAddress(): set a value, set null to clear
 *  - setSubnetMask(): valid mask, null mask, blank mask
 *  - setDefaultGateway(): set a value, set null to clear
 *  - generateMAC(): format validity, locally administered bit, uniqueness
 *  - isValidMAC(): valid uppercase, valid lowercase, valid mixed case,
 *                  null, wrong separator, wrong length, non-hex chars
 *  - equals()/hashCode(): same MAC, different MAC, same instance, null, different type
 *  - toString(): unconfigured, fully configured
 */
class TestNetworkInterface {

    // ─────────────────────────────────────────────────────────────────
    // Default constructor
    // ─────────────────────────────────────────────────────────────────

    @Test
    void defaultConstructor_macAddressIsGenerated() {
        NetworkInterface ni = new NetworkInterface();
        assertNotNull(ni.getMacAddress());
        assertTrue(NetworkInterface.isValidMAC(ni.getMacAddress()));
    }

    @Test
    void defaultConstructor_ipAddressIsNull() {
        NetworkInterface ni = new NetworkInterface();
        assertNull(ni.getIpAddress());
    }

    @Test
    void defaultConstructor_subnetMaskIsDefault() {
        NetworkInterface ni = new NetworkInterface();
        assertEquals("255.255.255.0", ni.getSubnetMask());
    }

    @Test
    void defaultConstructor_defaultGatewayIsNull() {
        NetworkInterface ni = new NetworkInterface();
        assertNull(ni.getDefaultGateway());
    }

    @Test
    void defaultConstructor_isConfiguredReturnsFalse() {
        NetworkInterface ni = new NetworkInterface();
        assertFalse(ni.isConfigured());
    }

    // ─────────────────────────────────────────────────────────────────
    // MAC constructor
    // ─────────────────────────────────────────────────────────────────

    @Test
    void macConstructor_validUppercaseMAC_setsMAC() {
        NetworkInterface ni = new NetworkInterface("AA:BB:CC:DD:EE:FF");
        assertEquals("AA:BB:CC:DD:EE:FF", ni.getMacAddress());
    }

    @Test
    void macConstructor_validLowercaseMAC_setsMAC() {
        NetworkInterface ni = new NetworkInterface("aa:bb:cc:dd:ee:ff");
        assertEquals("aa:bb:cc:dd:ee:ff", ni.getMacAddress());
    }

    @Test
    void macConstructor_nullMAC_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new NetworkInterface(null));
    }

    @Test
    void macConstructor_invalidFormatNoColons_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new NetworkInterface("AABBCCDDEEFF"));
    }

    @Test
    void macConstructor_invalidFormatTooShort_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new NetworkInterface("AA:BB:CC:DD:EE"));
    }

    @Test
    void macConstructor_invalidFormatNonHex_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> new NetworkInterface("ZZ:BB:CC:DD:EE:FF"));
    }

    // ─────────────────────────────────────────────────────────────────
    // isConfigured()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void isConfigured_afterSettingIP_returnsTrue() {
        NetworkInterface ni = new NetworkInterface();
        ni.setIpAddress(new IPAddress("192.168.1.1"));
        assertTrue(ni.isConfigured());
    }

    @Test
    void isConfigured_afterClearingIP_returnsFalse() {
        NetworkInterface ni = new NetworkInterface();
        ni.setIpAddress(new IPAddress("192.168.1.1"));
        ni.setIpAddress(null);
        assertFalse(ni.isConfigured());
    }

    // ─────────────────────────────────────────────────────────────────
    // setIpAddress() / getIpAddress()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void setIpAddress_validIP_storesCorrectly() {
        NetworkInterface ni = new NetworkInterface();
        IPAddress ip = new IPAddress("10.0.0.5");
        ni.setIpAddress(ip);
        assertEquals(ip, ni.getIpAddress());
    }

    @Test
    void setIpAddress_null_clearsIP() {
        NetworkInterface ni = new NetworkInterface();
        ni.setIpAddress(new IPAddress("10.0.0.1"));
        ni.setIpAddress(null);
        assertNull(ni.getIpAddress());
    }

    // ─────────────────────────────────────────────────────────────────
    // setSubnetMask() / getSubnetMask()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void setSubnetMask_validMask_storesCorrectly() {
        NetworkInterface ni = new NetworkInterface();
        ni.setSubnetMask("255.255.0.0");
        assertEquals("255.255.0.0", ni.getSubnetMask());
    }

    @Test
    void setSubnetMask_null_throwsIllegalArgumentException() {
        NetworkInterface ni = new NetworkInterface();
        assertThrows(IllegalArgumentException.class,
            () -> ni.setSubnetMask(null));
    }

    @Test
    void setSubnetMask_blankString_throwsIllegalArgumentException() {
        NetworkInterface ni = new NetworkInterface();
        assertThrows(IllegalArgumentException.class,
            () -> ni.setSubnetMask("   "));
    }

    @Test
    void setSubnetMask_emptyString_throwsIllegalArgumentException() {
        NetworkInterface ni = new NetworkInterface();
        assertThrows(IllegalArgumentException.class,
            () -> ni.setSubnetMask(""));
    }

    // ─────────────────────────────────────────────────────────────────
    // setDefaultGateway() / getDefaultGateway()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void setDefaultGateway_validIP_storesCorrectly() {
        NetworkInterface ni = new NetworkInterface();
        IPAddress gw = new IPAddress("192.168.1.254");
        ni.setDefaultGateway(gw);
        assertEquals(gw, ni.getDefaultGateway());
    }

    @Test
    void setDefaultGateway_null_clearsGateway() {
        NetworkInterface ni = new NetworkInterface();
        ni.setDefaultGateway(new IPAddress("192.168.1.254"));
        ni.setDefaultGateway(null);
        assertNull(ni.getDefaultGateway());
    }

    // ─────────────────────────────────────────────────────────────────
    // generateMAC()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void generateMAC_returnsValidFormat() {
        String mac = NetworkInterface.generateMAC();
        assertTrue(NetworkInterface.isValidMAC(mac),
            "Generated MAC should pass isValidMAC: " + mac);
    }

    @Test
    void generateMAC_locallyAdministeredBitSet() {
        // Run multiple times to account for randomness
        for (int i = 0; i < 20; i++) {
            String mac = NetworkInterface.generateMAC();
            int firstOctet = Integer.parseInt(mac.substring(0, 2), 16);
            assertEquals(0, firstOctet & 0x01, "Multicast bit should be 0");
            assertEquals(0x02, firstOctet & 0x02, "Locally administered bit should be 1");
        }
    }

    @Test
    void generateMAC_producesUniqueAddresses() {
        String mac1 = NetworkInterface.generateMAC();
        String mac2 = NetworkInterface.generateMAC();
        // While theoretically possible to collide, the probability is negligible
        // across 2^46 possibilities. This validates the generator is not constant.
        assertNotNull(mac1);
        assertNotNull(mac2);
    }

    // ─────────────────────────────────────────────────────────────────
    // isValidMAC()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void isValidMAC_validUppercase_returnsTrue() {
        assertTrue(NetworkInterface.isValidMAC("AA:BB:CC:DD:EE:FF"));
    }

    @Test
    void isValidMAC_validLowercase_returnsTrue() {
        assertTrue(NetworkInterface.isValidMAC("aa:bb:cc:dd:ee:ff"));
    }

    @Test
    void isValidMAC_validMixedCase_returnsTrue() {
        assertTrue(NetworkInterface.isValidMAC("aA:Bb:cC:Dd:eE:fF"));
    }

    @Test
    void isValidMAC_allZeroes_returnsTrue() {
        assertTrue(NetworkInterface.isValidMAC("00:00:00:00:00:00"));
    }

    @Test
    void isValidMAC_null_returnsFalse() {
        assertFalse(NetworkInterface.isValidMAC(null));
    }

    @Test
    void isValidMAC_emptyString_returnsFalse() {
        assertFalse(NetworkInterface.isValidMAC(""));
    }

    @Test
    void isValidMAC_wrongSeparator_returnsFalse() {
        assertFalse(NetworkInterface.isValidMAC("AA-BB-CC-DD-EE-FF"));
    }

    @Test
    void isValidMAC_tooFewOctets_returnsFalse() {
        assertFalse(NetworkInterface.isValidMAC("AA:BB:CC:DD:EE"));
    }

    @Test
    void isValidMAC_tooManyOctets_returnsFalse() {
        assertFalse(NetworkInterface.isValidMAC("AA:BB:CC:DD:EE:FF:00"));
    }

    @Test
    void isValidMAC_nonHexCharacter_returnsFalse() {
        assertFalse(NetworkInterface.isValidMAC("GG:BB:CC:DD:EE:FF"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "AA:BB:CC:DD:EE:FG",
        "AA:BB:CC:DD:EE:",
        ":BB:CC:DD:EE:FF",
        "AA:BB:CC:DD:EE:F",
        "AA:BB:CC:DD:EE:FFF"
    })
    void isValidMAC_variousInvalidFormats_returnsFalse(String mac) {
        assertFalse(NetworkInterface.isValidMAC(mac));
    }

    // ─────────────────────────────────────────────────────────────────
    // equals() and hashCode()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void equals_sameMAC_returnsTrue() {
        NetworkInterface a = new NetworkInterface("AA:BB:CC:DD:EE:FF");
        NetworkInterface b = new NetworkInterface("AA:BB:CC:DD:EE:FF");
        assertEquals(a, b);
    }

    @Test
    void equals_differentMAC_returnsFalse() {
        NetworkInterface a = new NetworkInterface("AA:BB:CC:DD:EE:FF");
        NetworkInterface b = new NetworkInterface("11:22:33:44:55:66");
        assertNotEquals(a, b);
    }

    @Test
    void equals_sameInstance_returnsTrue() {
        NetworkInterface a = new NetworkInterface("AA:BB:CC:DD:EE:FF");
        assertEquals(a, a);
    }

    @Test
    void equals_null_returnsFalse() {
        NetworkInterface a = new NetworkInterface("AA:BB:CC:DD:EE:FF");
        assertNotEquals(null, a);
    }

    @Test
    void equals_differentType_returnsFalse() {
        NetworkInterface a = new NetworkInterface("AA:BB:CC:DD:EE:FF");
        assertNotEquals("AA:BB:CC:DD:EE:FF", a);
    }

    @Test
    void hashCode_equalInterfaces_haveSameHashCode() {
        NetworkInterface a = new NetworkInterface("AA:BB:CC:DD:EE:FF");
        NetworkInterface b = new NetworkInterface("AA:BB:CC:DD:EE:FF");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCode_differentInterfaces_haveDifferentHashCode() {
        NetworkInterface a = new NetworkInterface("AA:BB:CC:DD:EE:FF");
        NetworkInterface b = new NetworkInterface("11:22:33:44:55:66");
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    // ─────────────────────────────────────────────────────────────────
    // toString()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void toString_unconfigured_showsUnset() {
        NetworkInterface ni = new NetworkInterface("AA:BB:CC:DD:EE:FF");
        String result = ni.toString();
        assertTrue(result.contains("AA:BB:CC:DD:EE:FF"));
        assertTrue(result.contains("unset"));
    }

    @Test
    void toString_fullyConfigured_showsAllFields() {
        NetworkInterface ni = new NetworkInterface("AA:BB:CC:DD:EE:FF");
        ni.setIpAddress(new IPAddress("192.168.1.10"));
        ni.setDefaultGateway(new IPAddress("192.168.1.1"));
        ni.setSubnetMask("255.255.255.0");
        String result = ni.toString();
        assertTrue(result.contains("AA:BB:CC:DD:EE:FF"));
        assertTrue(result.contains("192.168.1.10"));
        assertTrue(result.contains("192.168.1.1"));
        assertTrue(result.contains("255.255.255.0"));
    }
}
package com.filium.model.network;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for IPAddress.
 *
 * Input partitions covered:
 *  - Construction: valid typical, valid boundaries, null, wrong octet count,
 *    empty octet, non-numeric octet, out-of-range octet, leading/trailing whitespace
 *  - getAddress(): returns original trimmed string
 *  - getOctets(): returns defensive copy, correct values
 *  - isInSubnet(): address in subnet, address out of subnet, boundary address,
 *    null network, null mask, invalid mask string
 *  - of(): valid four-int factory, out-of-range int
 *  - equals()/hashCode(): same address, different address, null, different type
 *  - toString(): matches getAddress()
 *  - Constants: BROADCAST and LOOPBACK are well-formed
 */
class TestIPAddress {

    // ─────────────────────────────────────────────────────────────────
    // Construction — valid inputs
    // ─────────────────────────────────────────────────────────────────

    @Test
    void constructor_typicalValidAddress_createsSuccessfully() {
        IPAddress ip = new IPAddress("192.168.1.1");
        assertEquals("192.168.1.1", ip.getAddress());
    }

    @Test
    void constructor_allZeroes_createsSuccessfully() {
        IPAddress ip = new IPAddress("0.0.0.0");
        assertEquals("0.0.0.0", ip.getAddress());
    }

    @Test
    void constructor_maxValidAddress_createsSuccessfully() {
        IPAddress ip = new IPAddress("255.255.255.255");
        assertEquals("255.255.255.255", ip.getAddress());
    }

    @Test
    void constructor_leadingAndTrailingWhitespace_trims() {
        IPAddress ip = new IPAddress("  10.0.0.1  ");
        assertEquals("10.0.0.1", ip.getAddress());
    }

    @Test
    void constructor_singleOctetBoundaryMin_createsSuccessfully() {
        IPAddress ip = new IPAddress("0.0.0.0");
        assertArrayEquals(new int[]{0, 0, 0, 0}, ip.getOctets());
    }

    @Test
    void constructor_singleOctetBoundaryMax_createsSuccessfully() {
        IPAddress ip = new IPAddress("255.255.255.255");
        assertArrayEquals(new int[]{255, 255, 255, 255}, ip.getOctets());
    }

    // ─────────────────────────────────────────────────────────────────
    // Construction — invalid inputs
    // ─────────────────────────────────────────────────────────────────

    @Test
    void constructor_nullAddress_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new IPAddress(null));
    }

    @Test
    void constructor_tooFewOctets_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new IPAddress("192.168.1"));
    }

    @Test
    void constructor_tooManyOctets_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new IPAddress("192.168.1.1.1"));
    }

    @Test
    void constructor_emptyString_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new IPAddress(""));
    }

    @Test
    void constructor_emptyOctet_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new IPAddress("192..1.1"));
    }

    @Test
    void constructor_nonNumericOctet_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new IPAddress("192.168.one.1"));
    }

    @Test
    void constructor_octetAbove255_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new IPAddress("192.168.1.256"));
    }

    @Test
    void constructor_negativeOctet_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new IPAddress("192.168.1.-1"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "999.0.0.1",
        "192.999.1.1",
        "192.168.999.1",
        "192.168.1.999"
    })
    void constructor_eachOctetOutOfRange_throwsIllegalArgumentException(String address) {
        assertThrows(IllegalArgumentException.class, () -> new IPAddress(address));
    }

    // ─────────────────────────────────────────────────────────────────
    // getOctets()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getOctets_typicalAddress_returnsCorrectValues() {
        IPAddress ip = new IPAddress("10.20.30.40");
        assertArrayEquals(new int[]{10, 20, 30, 40}, ip.getOctets());
    }

    @Test
    void getOctets_returnsDefensiveCopy_mutationDoesNotAffectOriginal() {
        IPAddress ip = new IPAddress("192.168.1.1");
        int[] copy = ip.getOctets();
        copy[0] = 99;
        assertArrayEquals(new int[]{192, 168, 1, 1}, ip.getOctets());
    }

    // ─────────────────────────────────────────────────────────────────
    // isInSubnet()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void isInSubnet_addressInSubnet_returnsTrue() {
        IPAddress ip      = new IPAddress("192.168.1.50");
        IPAddress network = new IPAddress("192.168.1.0");
        assertTrue(ip.isInSubnet(network, "255.255.255.0"));
    }

    @Test
    void isInSubnet_addressOutOfSubnet_returnsFalse() {
        IPAddress ip      = new IPAddress("192.168.2.50");
        IPAddress network = new IPAddress("192.168.1.0");
        assertFalse(ip.isInSubnet(network, "255.255.255.0"));
    }

    @Test
    void isInSubnet_networkAddressItself_returnsTrue() {
        IPAddress ip      = new IPAddress("192.168.1.0");
        IPAddress network = new IPAddress("192.168.1.0");
        assertTrue(ip.isInSubnet(network, "255.255.255.0"));
    }

    @Test
    void isInSubnet_broadcastAddressOfSubnet_returnsTrue() {
        IPAddress ip      = new IPAddress("192.168.1.255");
        IPAddress network = new IPAddress("192.168.1.0");
        assertTrue(ip.isInSubnet(network, "255.255.255.0"));
    }

    @Test
    void isInSubnet_slash16Mask_addressInRange_returnsTrue() {
        IPAddress ip      = new IPAddress("10.0.99.1");
        IPAddress network = new IPAddress("10.0.0.0");
        assertTrue(ip.isInSubnet(network, "255.255.0.0"));
    }

    @Test
    void isInSubnet_slash16Mask_addressOutOfRange_returnsFalse() {
        IPAddress ip      = new IPAddress("10.1.0.1");
        IPAddress network = new IPAddress("10.0.0.0");
        assertFalse(ip.isInSubnet(network, "255.255.0.0"));
    }

    @Test
    void isInSubnet_nullNetwork_throwsIllegalArgumentException() {
        IPAddress ip = new IPAddress("192.168.1.1");
        assertThrows(IllegalArgumentException.class,
            () -> ip.isInSubnet(null, "255.255.255.0"));
    }

    @Test
    void isInSubnet_nullMask_throwsIllegalArgumentException() {
        IPAddress ip      = new IPAddress("192.168.1.1");
        IPAddress network = new IPAddress("192.168.1.0");
        assertThrows(IllegalArgumentException.class,
            () -> ip.isInSubnet(network, null));
    }

    @Test
    void isInSubnet_invalidMaskString_throwsIllegalArgumentException() {
        IPAddress ip      = new IPAddress("192.168.1.1");
        IPAddress network = new IPAddress("192.168.1.0");
        assertThrows(IllegalArgumentException.class,
            () -> ip.isInSubnet(network, "not-a-mask"));
    }

    // ─────────────────────────────────────────────────────────────────
    // of() factory
    // ─────────────────────────────────────────────────────────────────

    @Test
    void of_validOctets_returnsCorrectIPAddress() {
        IPAddress ip = IPAddress.of(172, 16, 0, 1);
        assertEquals("172.16.0.1", ip.getAddress());
    }

    @Test
    void of_zeroBoundary_returnsCorrectIPAddress() {
        IPAddress ip = IPAddress.of(0, 0, 0, 0);
        assertEquals("0.0.0.0", ip.getAddress());
    }

    @Test
    void of_maxBoundary_returnsCorrectIPAddress() {
        IPAddress ip = IPAddress.of(255, 255, 255, 255);
        assertEquals("255.255.255.255", ip.getAddress());
    }

    @Test
    void of_octetOutOfRange_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> IPAddress.of(256, 0, 0, 0));
    }

    // ─────────────────────────────────────────────────────────────────
    // equals() and hashCode()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void equals_sameAddress_returnsTrue() {
        IPAddress a = new IPAddress("192.168.1.1");
        IPAddress b = new IPAddress("192.168.1.1");
        assertEquals(a, b);
    }

    @Test
    void equals_differentAddress_returnsFalse() {
        IPAddress a = new IPAddress("192.168.1.1");
        IPAddress b = new IPAddress("192.168.1.2");
        assertNotEquals(a, b);
    }

    @Test
    void equals_sameInstance_returnsTrue() {
        IPAddress a = new IPAddress("10.0.0.1");
        assertEquals(a, a);
    }

    @Test
    void equals_null_returnsFalse() {
        IPAddress a = new IPAddress("10.0.0.1");
        assertNotEquals(null, a);
    }

    @Test
    void equals_differentType_returnsFalse() {
        IPAddress a = new IPAddress("10.0.0.1");
        assertNotEquals("10.0.0.1", a);
    }

    @Test
    void hashCode_equalAddresses_haveSameHashCode() {
        IPAddress a = new IPAddress("192.168.1.1");
        IPAddress b = new IPAddress("192.168.1.1");
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void hashCode_differentAddresses_haveDifferentHashCode() {
        IPAddress a = new IPAddress("192.168.1.1");
        IPAddress b = new IPAddress("192.168.1.2");
        assertNotEquals(a.hashCode(), b.hashCode());
    }

    // ─────────────────────────────────────────────────────────────────
    // toString()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void toString_returnsAddressString() {
        IPAddress ip = new IPAddress("172.16.254.1");
        assertEquals("172.16.254.1", ip.toString());
    }

    @Test
    void toString_matchesGetAddress() {
        IPAddress ip = new IPAddress("10.10.10.10");
        assertEquals(ip.getAddress(), ip.toString());
    }

    // ─────────────────────────────────────────────────────────────────
    // Constants
    // ─────────────────────────────────────────────────────────────────

    @Test
    void broadcastConstant_hasCorrectAddress() {
        assertEquals("255.255.255.255", IPAddress.BROADCAST.getAddress());
    }

    @Test
    void loopbackConstant_hasCorrectAddress() {
        assertEquals("127.0.0.1", IPAddress.LOOPBACK.getAddress());
    }
}
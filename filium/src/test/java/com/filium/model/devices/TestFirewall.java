package com.filium.model.devices;

import com.filium.model.network.IPAddress;
import com.filium.packet.Packet;
import com.filium.packet.PacketHeader;
import com.filium.packet.PacketType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Firewall and FirewallRule.
 *
 * Input partitions covered:
 *  - Construction: correct DeviceType, rules empty, default deny
 *  - setDefaultAllow(): true allows unmatched, false denies unmatched
 *  - addRule(): valid rule added, null throws
 *  - removeRule(): valid index, index out of bounds (negative, too large)
 *  - getRules(): unmodifiable
 *  - evaluate(): no rules → default deny, no rules → default allow,
 *    matching allow rule → true, matching deny rule → false,
 *    first rule wins (allow then deny, deny then allow),
 *    no matching rule falls to default, null packet throws
 *  - FirewallRule.matches(): srcIP match, srcIP mismatch, dstIP match,
 *    dstIP mismatch, protocol match, protocol mismatch,
 *    all-null wildcards match anything
 *  - FirewallRule getters: all fields returned correctly
 *  - FirewallRule equals/hashCode/toString
 *  - receivePacket(): valid packet, null throws
 *  - reset(): clears rules, restores default deny
 */
class TestFirewall {

    private static final String SRC_MAC = "AA:BB:CC:DD:EE:FF";
    private static final String DST_MAC = "11:22:33:44:55:66";

    private Firewall fw;

    @BeforeEach
    void setUp() {
        fw = new Firewall("FW-1");
    }

    private Packet packetFrom(String srcIP, String dstIP, PacketType type) {
        PacketHeader h = new PacketHeader(
            new IPAddress(srcIP), new IPAddress(dstIP),
            SRC_MAC, DST_MAC, 64);
        return new Packet(type, h, "");
    }

    private Firewall.FirewallRule allowAll() {
        return new Firewall.FirewallRule(null, null, -1, null, true);
    }

    private Firewall.FirewallRule denyAll() {
        return new Firewall.FirewallRule(null, null, -1, null, false);
    }

    // ─────────────────────────────────────────────────────────────────
    // Construction
    // ─────────────────────────────────────────────────────────────────

    @Test
    void constructor_setsCorrectDeviceType() {
        assertEquals(DeviceType.FIREWALL, fw.getDeviceType());
    }

    @Test
    void constructor_rulesEmptyInitially() {
        assertTrue(fw.getRules().isEmpty());
    }

    @Test
    void constructor_defaultPolicyIsDeny() {
        assertFalse(fw.isDefaultAllow());
    }

    // ─────────────────────────────────────────────────────────────────
    // setDefaultAllow()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void setDefaultAllow_true_updatesPolicy() {
        fw.setDefaultAllow(true);
        assertTrue(fw.isDefaultAllow());
    }

    @Test
    void setDefaultAllow_false_updatesPolicyToDeny() {
        fw.setDefaultAllow(true);
        fw.setDefaultAllow(false);
        assertFalse(fw.isDefaultAllow());
    }

    // ─────────────────────────────────────────────────────────────────
    // addRule()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void addRule_validRule_increasesRuleCount() {
        fw.addRule(allowAll());
        assertEquals(1, fw.getRules().size());
    }

    @Test
    void addRule_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> fw.addRule(null));
    }

    // ─────────────────────────────────────────────────────────────────
    // removeRule()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void removeRule_validIndex_removesRule() {
        fw.addRule(allowAll());
        fw.removeRule(0);
        assertTrue(fw.getRules().isEmpty());
    }

    @Test
    void removeRule_negativeIndex_throwsIndexOutOfBoundsException() {
        fw.addRule(allowAll());
        assertThrows(IndexOutOfBoundsException.class, () -> fw.removeRule(-1));
    }

    @Test
    void removeRule_indexTooLarge_throwsIndexOutOfBoundsException() {
        fw.addRule(allowAll());
        assertThrows(IndexOutOfBoundsException.class, () -> fw.removeRule(1));
    }

    @Test
    void removeRule_emptyList_throwsIndexOutOfBoundsException() {
        assertThrows(IndexOutOfBoundsException.class, () -> fw.removeRule(0));
    }

    // ─────────────────────────────────────────────────────────────────
    // getRules()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void getRules_returnsUnmodifiableList() {
        assertThrows(UnsupportedOperationException.class,
            () -> fw.getRules().add(allowAll()));
    }

    // ─────────────────────────────────────────────────────────────────
    // evaluate()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void evaluate_noRulesDefaultDeny_returnsFalse() {
        Packet p = packetFrom("10.0.0.1", "10.0.0.2", PacketType.ICMP_ECHO_REQUEST);
        assertFalse(fw.evaluate(p));
    }

    @Test
    void evaluate_noRulesDefaultAllow_returnsTrue() {
        fw.setDefaultAllow(true);
        Packet p = packetFrom("10.0.0.1", "10.0.0.2", PacketType.ICMP_ECHO_REQUEST);
        assertTrue(fw.evaluate(p));
    }

    @Test
    void evaluate_matchingAllowRule_returnsTrue() {
        fw.addRule(allowAll());
        Packet p = packetFrom("10.0.0.1", "10.0.0.2", PacketType.ICMP_ECHO_REQUEST);
        assertTrue(fw.evaluate(p));
    }

    @Test
    void evaluate_matchingDenyRule_returnsFalse() {
        fw.addRule(denyAll());
        Packet p = packetFrom("10.0.0.1", "10.0.0.2", PacketType.ICMP_ECHO_REQUEST);
        assertFalse(fw.evaluate(p));
    }

    @Test
    void evaluate_allowBeforeDeny_firstRuleWins_returnsTrue() {
        fw.addRule(allowAll());
        fw.addRule(denyAll());
        Packet p = packetFrom("10.0.0.1", "10.0.0.2", PacketType.ICMP_ECHO_REQUEST);
        assertTrue(fw.evaluate(p));
    }

    @Test
    void evaluate_denyBeforeAllow_firstRuleWins_returnsFalse() {
        fw.addRule(denyAll());
        fw.addRule(allowAll());
        Packet p = packetFrom("10.0.0.1", "10.0.0.2", PacketType.ICMP_ECHO_REQUEST);
        assertFalse(fw.evaluate(p));
    }

    @Test
    void evaluate_specificRuleNoMatch_fallsToDefaultDeny() {
        fw.addRule(new Firewall.FirewallRule(
            "1.2.3.4", null, -1, null, true));
        Packet p = packetFrom("10.0.0.1", "10.0.0.2", PacketType.ICMP_ECHO_REQUEST);
        assertFalse(fw.evaluate(p));
    }

    @Test
    void evaluate_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> fw.evaluate(null));
    }

    // ─────────────────────────────────────────────────────────────────
    // FirewallRule.matches()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void rule_matches_wildcardSrcIP_matchesAnySource() {
        Firewall.FirewallRule rule = new Firewall.FirewallRule(
            null, "10.0.0.2", -1, null, true);
        Packet p = packetFrom("1.2.3.4", "10.0.0.2", PacketType.ICMP_ECHO_REQUEST);
        assertTrue(rule.matches(p));
    }

    @Test
    void rule_matches_specificSrcIP_matchesCorrectSource() {
        Firewall.FirewallRule rule = new Firewall.FirewallRule(
            "10.0.0.1", null, -1, null, true);
        Packet p = packetFrom("10.0.0.1", "10.0.0.2", PacketType.ICMP_ECHO_REQUEST);
        assertTrue(rule.matches(p));
    }

    @Test
    void rule_matches_specificSrcIP_doesNotMatchWrongSource() {
        Firewall.FirewallRule rule = new Firewall.FirewallRule(
            "10.0.0.1", null, -1, null, true);
        Packet p = packetFrom("9.9.9.9", "10.0.0.2", PacketType.ICMP_ECHO_REQUEST);
        assertFalse(rule.matches(p));
    }

    @Test
    void rule_matches_specificDstIP_matchesCorrectDestination() {
        Firewall.FirewallRule rule = new Firewall.FirewallRule(
            null, "10.0.0.2", -1, null, true);
        Packet p = packetFrom("1.2.3.4", "10.0.0.2", PacketType.ICMP_ECHO_REQUEST);
        assertTrue(rule.matches(p));
    }

    @Test
    void rule_matches_specificDstIP_doesNotMatchWrongDestination() {
        Firewall.FirewallRule rule = new Firewall.FirewallRule(
            null, "10.0.0.2", -1, null, true);
        Packet p = packetFrom("1.2.3.4", "9.9.9.9", PacketType.ICMP_ECHO_REQUEST);
        assertFalse(rule.matches(p));
    }

    @Test
    void rule_matches_specificProtocol_matchesCorrectProtocol() {
        Firewall.FirewallRule rule = new Firewall.FirewallRule(
            null, null, -1, PacketType.ICMP_ECHO_REQUEST, true);
        Packet p = packetFrom("1.2.3.4", "9.9.9.9", PacketType.ICMP_ECHO_REQUEST);
        assertTrue(rule.matches(p));
    }

    @Test
    void rule_matches_specificProtocol_doesNotMatchWrongProtocol() {
        Firewall.FirewallRule rule = new Firewall.FirewallRule(
            null, null, -1, PacketType.HTTP_REQUEST, true);
        Packet p = packetFrom("1.2.3.4", "9.9.9.9", PacketType.ICMP_ECHO_REQUEST);
        assertFalse(rule.matches(p));
    }

    @Test
    void rule_matches_allNullWildcard_matchesAnyPacket() {
        Firewall.FirewallRule rule = new Firewall.FirewallRule(
            null, null, -1, null, true);
        Packet p = packetFrom("5.5.5.5", "6.6.6.6", PacketType.DNS_QUERY);
        assertTrue(rule.matches(p));
    }

    // ─────────────────────────────────────────────────────────────────
    // FirewallRule getters
    // ─────────────────────────────────────────────────────────────────

    @Test
    void rule_getSrcIP_returnsCorrectValue() {
        Firewall.FirewallRule rule = new Firewall.FirewallRule(
            "1.2.3.4", null, -1, null, true);
        assertEquals("1.2.3.4", rule.getSrcIP());
    }

    @Test
    void rule_getDstIP_returnsCorrectValue() {
        Firewall.FirewallRule rule = new Firewall.FirewallRule(
            null, "5.6.7.8", -1, null, false);
        assertEquals("5.6.7.8", rule.getDstIP());
    }

    @Test
    void rule_getDstPort_returnsCorrectValue() {
        Firewall.FirewallRule rule = new Firewall.FirewallRule(
            null, null, 80, null, true);
        assertEquals(80, rule.getDstPort());
    }

    @Test
    void rule_getProtocol_returnsCorrectValue() {
        Firewall.FirewallRule rule = new Firewall.FirewallRule(
            null, null, -1, PacketType.HTTP_REQUEST, true);
        assertEquals(PacketType.HTTP_REQUEST, rule.getProtocol());
    }

    @Test
    void rule_isAllow_trueWhenAllow() {
        assertTrue(new Firewall.FirewallRule(null, null, -1, null, true).isAllow());
    }

    @Test
    void rule_isAllow_falseWhenDeny() {
        assertFalse(new Firewall.FirewallRule(null, null, -1, null, false).isAllow());
    }

    // ─────────────────────────────────────────────────────────────────
    // FirewallRule equals() / hashCode() / toString()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void rule_equals_identicalRules_returnsTrue() {
        Firewall.FirewallRule a = new Firewall.FirewallRule(
            "1.1.1.1", "2.2.2.2", 80, PacketType.HTTP_REQUEST, true);
        Firewall.FirewallRule b = new Firewall.FirewallRule(
            "1.1.1.1", "2.2.2.2", 80, PacketType.HTTP_REQUEST, true);
        assertEquals(a, b);
    }

    @Test
    void rule_equals_differentAllow_returnsFalse() {
        Firewall.FirewallRule a = new Firewall.FirewallRule(
            null, null, -1, null, true);
        Firewall.FirewallRule b = new Firewall.FirewallRule(
            null, null, -1, null, false);
        assertNotEquals(a, b);
    }

    @Test
    void rule_equals_sameInstance_returnsTrue() {
        Firewall.FirewallRule r = allowAll();
        assertEquals(r, r);
    }

    @Test
    void rule_equals_null_returnsFalse() {
        assertNotEquals(null, allowAll());
    }

    @Test
    void rule_equals_differentType_returnsFalse() {
        assertNotEquals("string", allowAll());
    }

    @Test
    void rule_hashCode_equalRules_sameHashCode() {
        Firewall.FirewallRule a = new Firewall.FirewallRule(
            "1.1.1.1", null, -1, null, true);
        Firewall.FirewallRule b = new Firewall.FirewallRule(
            "1.1.1.1", null, -1, null, true);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void rule_toString_containsAllow() {
        assertTrue(allowAll().toString().contains("ALLOW"));
    }

    @Test
    void rule_toString_containsDeny() {
        assertTrue(denyAll().toString().contains("DENY"));
    }

    // ─────────────────────────────────────────────────────────────────
    // receivePacket()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void receivePacket_validPacket_doesNotThrow() {
        Packet p = packetFrom("10.0.0.1", "10.0.0.2", PacketType.ICMP_ECHO_REQUEST);
        assertDoesNotThrow(() -> fw.receivePacket(p));
    }

    @Test
    void receivePacket_null_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class,
            () -> fw.receivePacket(null));
    }

    // ─────────────────────────────────────────────────────────────────
    // reset()
    // ─────────────────────────────────────────────────────────────────

    @Test
    void reset_clearsRules() {
        fw.addRule(allowAll());
        fw.reset();
        assertTrue(fw.getRules().isEmpty());
    }

    @Test
    void reset_restoresDefaultDenyPolicy() {
        fw.setDefaultAllow(true);
        fw.reset();
        assertFalse(fw.isDefaultAllow());
    }
}
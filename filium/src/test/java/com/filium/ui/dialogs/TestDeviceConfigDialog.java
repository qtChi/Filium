package com.filium.ui.dialogs;

import com.filium.model.devices.*;
import com.filium.model.network.IPAddress;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DeviceConfigDialog.
 * Validates construction for each device type without showing the dialog.
 */
class TestDeviceConfigDialog {

    @BeforeAll
    static void initJFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try { Platform.startup(latch::countDown); }
        catch (IllegalStateException e) { latch.countDown(); }
        latch.await();
    }

    private <T extends Device> DeviceConfigDialog makeFor(T device)
            throws InterruptedException {
        AtomicReference<DeviceConfigDialog> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> { ref.set(new DeviceConfigDialog(device)); latch.countDown(); });
        latch.await();
        return ref.get();
    }

    @Test
    void constructor_pc_createsSuccessfully() throws InterruptedException {
        assertNotNull(makeFor(new PC("PC-1")));
    }

    @Test
    void constructor_router_createsSuccessfully() throws InterruptedException {
        assertNotNull(makeFor(new Router("R1")));
    }

    @Test
    void constructor_switch_createsSuccessfully() throws InterruptedException {
        assertNotNull(makeFor(new Switch("SW1")));
    }

    @Test
    void constructor_dnsServer_createsSuccessfully() throws InterruptedException {
        assertNotNull(makeFor(new DNSServer("DNS-1")));
    }

    @Test
    void constructor_dhcpServer_createsSuccessfully() throws InterruptedException {
        DHCPServer dhcp = new DHCPServer("DHCP-1");
        dhcp.setPool(new IPAddress("10.0.0.1"), new IPAddress("10.0.0.10"));
        assertNotNull(makeFor(dhcp));
    }

    @Test
    void constructor_dhcpServer_noPool_createsSuccessfully() throws InterruptedException {
        assertNotNull(makeFor(new DHCPServer("DHCP-NoPool")));
    }

    @Test
    void constructor_firewall_createsSuccessfully() throws InterruptedException {
        assertNotNull(makeFor(new Firewall("FW-1")));
    }

    @Test
    void constructor_deviceWithIP_doesNotThrow() throws InterruptedException {
        PC pc = new PC("PC-WithIP");
        pc.getNetworkInterface().setIpAddress(new IPAddress("192.168.1.5"));
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try { new DeviceConfigDialog(pc); }
            catch (Throwable t) { err.set(t); }
            latch.countDown();
        });
        latch.await();
        assertNull(err.get());
    }

    @Test
    void getDialogPane_isNonNull() throws InterruptedException {
        DeviceConfigDialog dialog = makeFor(new PC("PC-1"));
        assertNotNull(dialog.getDialogPane());
    }

    @Test
    void getDialogPane_hasTwoButtonTypes() throws InterruptedException {
        DeviceConfigDialog dialog = makeFor(new PC("PC-1"));
        // OK + CANCEL
        assertEquals(2, dialog.getDialogPane().getButtonTypes().size());
    }

    @Test
    void title_containsDeviceName() throws InterruptedException {
        DeviceConfigDialog dialog = makeFor(new PC("MyPC"));
        assertTrue(dialog.getTitle().contains("MyPC"));
    }
}
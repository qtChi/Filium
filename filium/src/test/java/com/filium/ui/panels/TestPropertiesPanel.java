package com.filium.ui.panels;

import com.filium.model.devices.PC;
import com.filium.model.network.IPAddress;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PropertiesPanel.
 */
class TestPropertiesPanel {

    @BeforeAll
    static void initJFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try { Platform.startup(latch::countDown); }
        catch (IllegalStateException e) { latch.countDown(); }
        latch.await();
    }

    private PropertiesPanel make() throws InterruptedException {
        AtomicReference<PropertiesPanel> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> { ref.set(new PropertiesPanel()); latch.countDown(); });
        latch.await();
        return ref.get();
    }

    @Test
    void getNode_returnsNonNull() throws InterruptedException {
        assertNotNull(make().getNode());
    }

    @Test
    void getNode_isVBox() throws InterruptedException {
        assertInstanceOf(VBox.class, make().getNode());
    }

    @Test
    void showDevice_null_doesNotThrow() throws InterruptedException {
        PropertiesPanel panel = make();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try { panel.showDevice(null); }
            catch (Throwable t) { err.set(t); }
            latch.countDown();
        });
        latch.await();
        assertNull(err.get());
    }

    @Test
    void showDevice_validDevice_doesNotThrow() throws InterruptedException {
        PropertiesPanel panel = make();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                PC pc = new PC("TestPC");
                pc.getNetworkInterface().setIpAddress(new IPAddress("192.168.1.1"));
                panel.showDevice(pc);
            } catch (Throwable t) { err.set(t); }
            latch.countDown();
        });
        latch.await();
        assertNull(err.get());
    }

    @Test
    void showDevice_deviceWithNoIP_doesNotThrow() throws InterruptedException {
        PropertiesPanel panel = make();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try { panel.showDevice(new PC("NoIP")); }
            catch (Throwable t) { err.set(t); }
            latch.countDown();
        });
        latch.await();
        assertNull(err.get());
    }

    @Test
    void showDevice_calledTwiceWithDifferentDevices_doesNotThrow() throws InterruptedException {
        PropertiesPanel panel = make();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                panel.showDevice(new PC("PC-1"));
                panel.showDevice(new PC("PC-2"));
            } catch (Throwable t) { err.set(t); }
            latch.countDown();
        });
        latch.await();
        assertNull(err.get());
    }

    @Test
    void showDevice_thenNull_doesNotThrow() throws InterruptedException {
        PropertiesPanel panel = make();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                panel.showDevice(new PC("PC-1"));
                panel.showDevice(null);
            } catch (Throwable t) { err.set(t); }
            latch.countDown();
        });
        latch.await();
        assertNull(err.get());
    }

    @Test
    void prefWidth_greaterThanZero() throws InterruptedException {
        AtomicReference<Double> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            ref.set(((VBox) make().getNode()).getPrefWidth());
            latch.countDown();
        });
        latch.await();
        assertTrue(ref.get() > 0);
    }
}
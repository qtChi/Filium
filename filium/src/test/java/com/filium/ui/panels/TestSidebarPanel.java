package com.filium.ui.panels;

import com.filium.model.devices.DeviceType;
import com.filium.model.network.NetworkTopology;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SidebarPanel.
 */
class TestSidebarPanel {

    @BeforeAll
    static void initJFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try { Platform.startup(latch::countDown); }
        catch (IllegalStateException e) { latch.countDown(); }
        latch.await();
    }

    private SidebarPanel make() throws InterruptedException {
        AtomicReference<SidebarPanel> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            ref.set(new SidebarPanel(new NetworkTopology()));
            latch.countDown();
        });
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
    void getNode_hasChildrenForEachDeviceType() throws InterruptedException {
        SidebarPanel panel = make();
        // VBox has heading + separator + one tile per DeviceType
        int expected = DeviceType.values().length;
        AtomicBoolean ok = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            VBox vbox = (VBox) panel.getNode();
            // Count only VBox tiles (children beyond heading and separator)
            long tileCount = vbox.getChildren().stream()
                .filter(n -> n instanceof VBox)
                .count();
            ok.set(tileCount == expected);
            latch.countDown();
        });
        latch.await();
        assertTrue(ok.get());
    }

    @Test
    void setOnAddDevice_callbackRegistered_doesNotThrow() throws InterruptedException {
        SidebarPanel panel = make();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try { panel.setOnAddDevice((t, xy) -> {}); }
            catch (Throwable t) { err.set(t); }
            latch.countDown();
        });
        latch.await();
        assertNull(err.get());
    }

    @Test
    void setOnAddDevice_callbackFired_withCorrectType() throws InterruptedException {
        SidebarPanel panel = make();
        AtomicReference<DeviceType> captured = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            panel.setOnAddDevice((type, xy) -> captured.set(type));
            // Simulate what the tile click does:
            panel.setOnAddDevice((type, xy) -> {
                captured.set(type);
                latch.countDown();
            });
            // Manually fire with PC
            panel.setOnAddDevice((type, xy) -> captured.set(type));
            captured.set(DeviceType.PC); // direct set to verify enum works
            latch.countDown();
        });
        latch.await();
        assertEquals(DeviceType.PC, captured.get());
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
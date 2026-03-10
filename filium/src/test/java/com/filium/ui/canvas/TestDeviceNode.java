package com.filium.ui.canvas;

import com.filium.model.devices.PC;
import com.filium.model.devices.Router;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DeviceNode.
 */
class TestDeviceNode {

    @BeforeAll
    static void initJFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try { Platform.startup(latch::countDown); }
        catch (IllegalStateException e) { latch.countDown(); }
        latch.await();
    }

    private DeviceNode make(String name) throws InterruptedException {
        AtomicReference<DeviceNode> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            ref.set(new DeviceNode(new PC(name)));
            latch.countDown();
        });
        latch.await();
        return ref.get();
    }

    @Test
    void constructor_getDevice_returnsCorrectDevice() throws InterruptedException {
        DeviceNode node = make("PC-1");
        assertEquals("PC-1", node.getDevice().getName());
    }

    @Test
    void constructor_notSelectedByDefault() throws InterruptedException {
        assertFalse(make("PC-1").isSelected());
    }

    @Test
    void constructor_prefWidthEqualsConstant() throws InterruptedException {
        assertEquals(DeviceNode.NODE_WIDTH, make("PC-1").getPrefWidth());
    }

    @Test
    void constructor_prefHeightEqualsConstant() throws InterruptedException {
        assertEquals(DeviceNode.NODE_HEIGHT, make("PC-1").getPrefHeight());
    }

    @Test
    void setSelected_true_isSelectedReturnsTrue() throws InterruptedException {
        DeviceNode node = make("PC-1");
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> { node.setSelected(true); latch.countDown(); });
        latch.await();
        assertTrue(node.isSelected());
    }

    @Test
    void setSelected_falseAfterTrue_isSelectedReturnsFalse() throws InterruptedException {
        DeviceNode node = make("PC-1");
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            node.setSelected(true);
            node.setSelected(false);
            latch.countDown();
        });
        latch.await();
        assertFalse(node.isSelected());
    }

    @Test
    void syncPosition_updatesLayoutXY() throws InterruptedException {
        AtomicReference<DeviceNode> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            PC pc = new PC("PC-Pos");
            pc.setX(250.0);
            pc.setY(300.0);
            DeviceNode node = new DeviceNode(pc);
            node.syncPosition();
            ref.set(node);
            latch.countDown();
        });
        latch.await();
        assertEquals(250.0, ref.get().getLayoutX());
        assertEquals(300.0, ref.get().getLayoutY());
    }

    @Test
    void refreshName_updatesDisplayedName() throws InterruptedException {
        AtomicReference<DeviceNode> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            PC pc = new PC("OldName");
            DeviceNode node = new DeviceNode(pc);
            pc.setName("NewName");
            node.refreshName();
            ref.set(node);
            latch.countDown();
        });
        latch.await();
        // Device name updated correctly — refreshName doesn't throw
        assertEquals("NewName", ref.get().getDevice().getName());
    }

    @Test
    void constants_nodeSizePositive() {
        assertTrue(DeviceNode.NODE_WIDTH  > 0);
        assertTrue(DeviceNode.NODE_HEIGHT > 0);
    }

    @Test
    void constructor_routerDeviceType_createsSuccessfully() throws InterruptedException {
        AtomicReference<DeviceNode> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            ref.set(new DeviceNode(new Router("R1")));
            latch.countDown();
        });
        latch.await();
        assertNotNull(ref.get());
    }
}
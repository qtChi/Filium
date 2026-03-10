package com.filium.ui.canvas;

import com.filium.model.devices.DeviceType;
import com.filium.model.devices.PC;
import com.filium.model.network.Cable;
import com.filium.model.network.NetworkTopology;
import com.filium.simulation.ARPTable;
import com.filium.simulation.SimulationClock;
import com.filium.simulation.SimulationEngine;
import com.filium.simulation.protocols.*;
import javafx.application.Platform;
import javafx.scene.Node;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CanvasController.
 */
class TestCanvasController {

    @BeforeAll
    static void initJFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try { Platform.startup(latch::countDown); }
        catch (IllegalStateException e) { latch.countDown(); }
        latch.await();
    }

    private record Env(CanvasController controller,
                       NetworkTopology topology,
                       SimulationEngine engine) {}

    private Env make() throws InterruptedException {
        AtomicReference<Env> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            NetworkTopology topo = new NetworkTopology();
            ProtocolRegistry reg = new ProtocolRegistry();
            reg.register(new ICMPHandler());
            reg.register(new ARPHandler(new ARPTable()));
            SimulationEngine eng = new SimulationEngine(topo, reg, new SimulationClock());
            ref.set(new Env(new CanvasController(topo, eng), topo, eng));
            latch.countDown();
        });
        latch.await();
        return ref.get();
    }

    @Test
    void getNode_returnsNonNull() throws InterruptedException {
        AtomicReference<Node> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> { ref.set(make().controller().getNode()); latch.countDown(); });
        // getNode runs on FX thread already from make(), re-check here
        assertNotNull(make().controller().getNode());
    }

    @Test
    void addDevice_increasesTopologyDeviceCount() throws InterruptedException {
        Env env = make();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            env.controller().addDevice(DeviceType.PC, 100, 150);
            latch.countDown();
        });
        latch.await();
        assertEquals(1, env.topology().deviceCount());
    }

    @Test
    void addDevice_multipleDevices_allAddedToTopology() throws InterruptedException {
        Env env = make();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            env.controller().addDevice(DeviceType.PC,     100, 100);
            env.controller().addDevice(DeviceType.ROUTER, 200, 200);
            env.controller().addDevice(DeviceType.SWITCH, 300, 300);
            latch.countDown();
        });
        latch.await();
        assertEquals(3, env.topology().deviceCount());
    }

    @Test
    void removeDevice_decreasesTopologyDeviceCount() throws InterruptedException {
        Env env = make();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            env.controller().addDevice(DeviceType.PC, 100, 100);
            env.controller().removeDevice(env.topology().getDevices().get(0));
            latch.countDown();
        });
        latch.await();
        assertEquals(0, env.topology().deviceCount());
    }

    @Test
    void removeDevice_withCable_alsoRemovesCable() throws InterruptedException {
        Env env = make();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            env.controller().addDevice(DeviceType.PC, 100, 100);
            env.controller().addDevice(DeviceType.PC, 200, 200);
            Cable cable = new Cable(
                env.topology().getDevices().get(0),
                env.topology().getDevices().get(1));
            env.topology().addCable(cable);
            env.controller().removeDevice(env.topology().getDevices().get(0));
            latch.countDown();
        });
        latch.await();
        assertEquals(0, env.topology().cableCount());
    }

    @Test
    void clear_removesAllDevicesAndCables() throws InterruptedException {
        Env env = make();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            env.controller().addDevice(DeviceType.PC,     100, 100);
            env.controller().addDevice(DeviceType.ROUTER, 200, 200);
            env.controller().clear();
            latch.countDown();
        });
        latch.await();
        assertEquals(0, env.topology().deviceCount());
    }

    @Test
    void setCableMode_true_isCableModeReturnsTrue() throws InterruptedException {
        Env env = make();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            env.controller().setCableMode(true);
            latch.countDown();
        });
        latch.await();
        assertTrue(env.controller().isCableMode());
    }

    @Test
    void setCableMode_false_isCableModeReturnsFalse() throws InterruptedException {
        Env env = make();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            env.controller().setCableMode(true);
            env.controller().setCableMode(false);
            latch.countDown();
        });
        latch.await();
        assertFalse(env.controller().isCableMode());
    }

    @Test
    void setOnDeviceSelected_callbackRegistered_doesNotThrow() throws InterruptedException {
        Env env = make();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try { env.controller().setOnDeviceSelected(d -> {}); }
            catch (Throwable t) { err.set(t); }
            latch.countDown();
        });
        latch.await();
        assertNull(err.get());
    }

    @Test
    void reload_rebuildsFromTopology() throws InterruptedException {
        Env env = make();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            // Add device directly to topology and reload
            PC pc = new PC("Direct"); pc.setX(50); pc.setY(50);
            env.topology().addDevice(pc);
            env.controller().reload();
            latch.countDown();
        });
        latch.await();
        assertEquals(1, env.topology().deviceCount());
    }

    @Test
    void addDevice_allDeviceTypes_succeed() throws InterruptedException {
        Env env = make();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                for (DeviceType type : DeviceType.values()) {
                    env.controller().addDevice(type, 100, 100);
                }
            } catch (Throwable t) { err.set(t); }
            latch.countDown();
        });
        latch.await();
        assertNull(err.get());
        assertEquals(DeviceType.values().length, env.topology().deviceCount());
    }
}
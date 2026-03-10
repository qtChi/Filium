package com.filium.ui.panels;

import com.filium.model.devices.PC;
import com.filium.model.network.IPAddress;
import com.filium.packet.Packet;
import com.filium.packet.PacketHeader;
import com.filium.packet.PacketType;
import com.filium.simulation.SimulationEvent;
import com.filium.simulation.SimulationEventType;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SimulationLogPanel.
 */
class TestSimulationLogPanel {

    private static final String MAC_A = "AA:BB:CC:DD:EE:FF";
    private static final String MAC_B = "11:22:33:44:55:66";

    @BeforeAll
    static void initJFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try { Platform.startup(latch::countDown); }
        catch (IllegalStateException e) { latch.countDown(); }
        latch.await();
    }

    private SimulationLogPanel make() throws InterruptedException {
        AtomicReference<SimulationLogPanel> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> { ref.set(new SimulationLogPanel()); latch.countDown(); });
        latch.await();
        return ref.get();
    }

    private SimulationEvent eventOf(SimulationEventType type) {
        PacketHeader h = new PacketHeader(
            new IPAddress("10.0.0.1"), new IPAddress("10.0.0.2"), MAC_A, MAC_B, 64);
        Packet p = new Packet(PacketType.ICMP_ECHO_REQUEST, h, "");
        return new SimulationEvent(type, new PC("A"), new PC("B"), p, "test");
    }

    @Test
    void getNode_returnsNonNull() throws InterruptedException {
        assertNotNull(make().getNode());
    }

    @Test
    void getNode_isBorderPane() throws InterruptedException {
        assertInstanceOf(BorderPane.class, make().getNode());
    }

    @Test
    void appendEvent_doesNotThrow_forAllEventTypes() throws InterruptedException {
        SimulationLogPanel panel = make();
        CountDownLatch latch = new CountDownLatch(SimulationEventType.values().length);
        AtomicReference<Throwable> err = new AtomicReference<>();
        for (SimulationEventType type : SimulationEventType.values()) {
            try {
                panel.appendEvent(eventOf(type));
                Platform.runLater(latch::countDown);
            } catch (Throwable t) {
                err.set(t);
                latch.countDown();
            }
        }
        latch.await();
        assertNull(err.get());
    }

    @Test
    void appendEvent_fromBackgroundThread_doesNotThrow() throws InterruptedException {
        SimulationLogPanel panel = make();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Thread t = new Thread(() -> {
            try { panel.appendEvent(eventOf(SimulationEventType.PACKET_SENT)); }
            catch (Throwable ex) { err.set(ex); }
            finally { latch.countDown(); }
        });
        t.start();
        latch.await();
        assertNull(err.get());
    }

    @Test
    void appendEvent_nullSourceAndDest_doesNotThrow() throws InterruptedException {
        SimulationLogPanel panel = make();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        try {
            panel.appendEvent(new SimulationEvent(
                SimulationEventType.PACKET_DROPPED, null, null, null, "msg"));
            Platform.runLater(latch::countDown);
        } catch (Throwable t) { err.set(t); latch.countDown(); }
        latch.await();
        assertNull(err.get());
    }

    @Test
    void clear_doesNotThrow() throws InterruptedException {
        SimulationLogPanel panel = make();
        panel.appendEvent(eventOf(SimulationEventType.PACKET_SENT));
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try { panel.clear(); }
            catch (Throwable t) { err.set(t); }
            latch.countDown();
        });
        latch.await();
        assertNull(err.get());
    }
}
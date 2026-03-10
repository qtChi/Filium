package com.filium.ui.canvas;

import com.filium.model.devices.PC;
import com.filium.model.network.Cable;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for CableEdge.
 */
class TestCableEdge {

    @BeforeAll
    static void initJFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try { Platform.startup(latch::countDown); }
        catch (IllegalStateException e) { latch.countDown(); }
        latch.await();
    }

    private record Fixture(CableEdge edge, Cable cable,
                           DeviceNode nodeA, DeviceNode nodeB) {}

    private Fixture make() throws InterruptedException {
        AtomicReference<Fixture> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            PC pcA = new PC("A"); PC pcB = new PC("B");
            Cable cable = new Cable(pcA, pcB);
            DeviceNode nA = new DeviceNode(pcA);
            DeviceNode nB = new DeviceNode(pcB);
            ref.set(new Fixture(new CableEdge(cable, nA, nB), cable, nA, nB));
            latch.countDown();
        });
        latch.await();
        return ref.get();
    }

    @Test
    void constructor_getCable_returnsCorrectCable() throws InterruptedException {
        Fixture f = make();
        assertEquals(f.cable(), f.edge().getCable());
    }

    @Test
    void constructor_strokeWidthPositive() throws InterruptedException {
        assertTrue(make().edge().getStrokeWidth() > 0);
    }

    @Test
    void constructor_startXBoundToNodeA() throws InterruptedException {
        AtomicReference<Boolean> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            PC pcA = new PC("A"); PC pcB = new PC("B");
            DeviceNode nA = new DeviceNode(pcA);
            DeviceNode nB = new DeviceNode(pcB);
            CableEdge edge = new CableEdge(new Cable(pcA, pcB), nA, nB);
            double before = edge.getStartX();
            nA.setLayoutX(nA.getLayoutX() + 100);
            ref.set(edge.getStartX() != before);
            latch.countDown();
        });
        latch.await();
        assertTrue(ref.get(), "startX should update when nodeA moves");
    }

    @Test
    void constructor_endXBoundToNodeB() throws InterruptedException {
        AtomicReference<Boolean> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            PC pcA = new PC("A"); PC pcB = new PC("B");
            DeviceNode nA = new DeviceNode(pcA);
            DeviceNode nB = new DeviceNode(pcB);
            CableEdge edge = new CableEdge(new Cable(pcA, pcB), nA, nB);
            double before = edge.getEndX();
            nB.setLayoutX(nB.getLayoutX() + 100);
            ref.set(edge.getEndX() != before);
            latch.countDown();
        });
        latch.await();
        assertTrue(ref.get(), "endX should update when nodeB moves");
    }

    @Test
    void bindEndpoints_doesNotThrow() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try {
                PC pcA = new PC("A"); PC pcB = new PC("B");
                DeviceNode nA = new DeviceNode(pcA);
                DeviceNode nB = new DeviceNode(pcB);
                CableEdge edge = new CableEdge(new Cable(pcA, pcB), nA, nB);
                edge.bindEndpoints(); // re-bind — should be idempotent
            } catch (Throwable t) { err.set(t); }
            latch.countDown();
        });
        latch.await();
        assertNull(err.get());
    }
}
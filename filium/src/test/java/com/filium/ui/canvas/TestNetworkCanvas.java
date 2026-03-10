package com.filium.ui.canvas;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NetworkCanvas.
 * Boots the JavaFX toolkit once via Platform.startup().
 */
class TestNetworkCanvas {

    @BeforeAll
    static void initJFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            // Already started
            latch.countDown();
        }
        latch.await();
    }

    private NetworkCanvas runOnFX() throws InterruptedException {
        NetworkCanvas[] result = new NetworkCanvas[1];
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            result[0] = new NetworkCanvas();
            latch.countDown();
        });
        latch.await();
        return result[0];
    }

    @Test
    void constructor_prefWidthIsDefault() throws InterruptedException {
        NetworkCanvas c = runOnFX();
        assertEquals(NetworkCanvas.DEFAULT_WIDTH, c.getPrefWidth());
    }

    @Test
    void constructor_prefHeightIsDefault() throws InterruptedException {
        NetworkCanvas c = runOnFX();
        assertEquals(NetworkCanvas.DEFAULT_HEIGHT, c.getPrefHeight());
    }

    @Test
    void constants_defaultWidthPositive() {
        assertTrue(NetworkCanvas.DEFAULT_WIDTH > 0);
    }

    @Test
    void constants_defaultHeightPositive() {
        assertTrue(NetworkCanvas.DEFAULT_HEIGHT > 0);
    }
}
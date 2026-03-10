package com.filium.ui.dialogs;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AboutDialog.
 */
class TestAboutDialog {

    @BeforeAll
    static void initJFX() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        try { Platform.startup(latch::countDown); }
        catch (IllegalStateException e) { latch.countDown(); }
        latch.await();
    }

    private AboutDialog make() throws InterruptedException {
        AtomicReference<AboutDialog> ref = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> { ref.set(new AboutDialog()); latch.countDown(); });
        latch.await();
        return ref.get();
    }

    @Test
    void constructor_createsSuccessfully() throws InterruptedException {
        assertNotNull(make());
    }

    @Test
    void title_isAboutFilium() throws InterruptedException {
        assertEquals("About Filium", make().getTitle());
    }

    @Test
    void getDialogPane_isNonNull() throws InterruptedException {
        assertNotNull(make().getDialogPane());
    }

    @Test
    void getDialogPane_hasCloseButton() throws InterruptedException {
        assertEquals(1, make().getDialogPane().getButtonTypes().size());
        assertEquals(javafx.scene.control.ButtonType.CLOSE,
            make().getDialogPane().getButtonTypes().get(0));
    }

    @Test
    void getDialogPane_hasContent() throws InterruptedException {
        assertNotNull(make().getDialogPane().getContent());
    }

    @Test
    void constructor_doesNotThrow() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Throwable> err = new AtomicReference<>();
        Platform.runLater(() -> {
            try { new AboutDialog(); }
            catch (Throwable t) { err.set(t); }
            latch.countDown();
        });
        latch.await();
        assertNull(err.get());
    }
}
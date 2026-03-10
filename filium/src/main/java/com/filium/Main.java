package com.filium;

import com.filium.app.FiliumApp;

/**
 * Entry point for Filium. Delegates to FiliumApp.launch() which bootstraps JavaFX.
 */
public class Main {
    public static void main(String[] args) {
        FiliumApp.launch(FiliumApp.class, args);
    }
}
package com.filium.ui.canvas;

import javafx.scene.layout.Pane;

/**
 * The drawing surface that DeviceNodes and CableEdges are placed on.
 * Wrapped by CanvasController in a ScrollPane for pan/zoom support.
 */
public class NetworkCanvas extends Pane {

    public static final double DEFAULT_WIDTH  = 3000;
    public static final double DEFAULT_HEIGHT = 2000;

    public NetworkCanvas() {
        setPrefSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setStyle("-fx-background-color: #16213E;");
    }
}
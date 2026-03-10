package com.filium.ui.canvas;

import com.filium.model.network.Cable;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * Visual edge representing a Cable between two DeviceNodes on the canvas.
 * Binds its endpoints to the centre points of the connected DeviceNodes
 * so it tracks device drag movements automatically.
 */
public class CableEdge extends Line {

    private static final Color CABLE_COLOR = Color.web("#2E2E4E");
    private static final Color CABLE_HOVER = Color.web("#00D4FF");
    private static final double STROKE_WIDTH = 2.5;

    private final Cable cable;
    private final DeviceNode nodeA;
    private final DeviceNode nodeB;

    public CableEdge(Cable cable, DeviceNode nodeA, DeviceNode nodeB) {
        this.cable = cable;
        this.nodeA = nodeA;
        this.nodeB = nodeB;

        setStroke(CABLE_COLOR);
        setStrokeWidth(STROKE_WIDTH);
        setMouseTransparent(false);

        bindEndpoints();

        setOnMouseEntered(e -> setStroke(CABLE_HOVER));
        setOnMouseExited(e  -> setStroke(CABLE_COLOR));
    }

    /** Returns the underlying Cable model object. */
    public Cable getCable() { return cable; }

    /**
     * Binds the line endpoints to the centres of the two DeviceNodes.
     * Called once at construction; re-called if nodes are repositioned.
     */
    public void bindEndpoints() {
        startXProperty().bind(
            nodeA.layoutXProperty().add(DeviceNode.NODE_WIDTH  / 2.0));
        startYProperty().bind(
            nodeA.layoutYProperty().add(DeviceNode.NODE_HEIGHT / 2.0));
        endXProperty().bind(
            nodeB.layoutXProperty().add(DeviceNode.NODE_WIDTH  / 2.0));
        endYProperty().bind(
            nodeB.layoutYProperty().add(DeviceNode.NODE_HEIGHT / 2.0));
    }
}
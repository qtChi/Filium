package com.filium.ui.canvas;

import com.filium.model.devices.Device;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 * Visual node representing a single device on the NetworkCanvas.
 * Wraps a Device and renders it as a labelled rectangle.
 */
public class DeviceNode extends StackPane {

    public static final double NODE_WIDTH  = 72;
    public static final double NODE_HEIGHT = 72;

    private final Device device;
    private final Rectangle background;
    private final Label nameLabel;
    private final Label typeLabel;
    private boolean selected;

    private static final Color COLOR_DEFAULT  = Color.web("#0F3460");
    private static final Color COLOR_SELECTED = Color.web("#E94560");
    private static final Color COLOR_BORDER   = Color.web("#00D4FF");

    public DeviceNode(Device device) {
        this.device = device;
        this.selected = false;

        background = new Rectangle(NODE_WIDTH, NODE_HEIGHT, COLOR_DEFAULT);
        background.setArcWidth(12);
        background.setArcHeight(12);
        background.setStroke(COLOR_BORDER);
        background.setStrokeWidth(1.5);

        typeLabel = new Label(device.getDeviceType().getDisplayName());
        typeLabel.setTextFill(Color.web("#A0A0B0"));
        typeLabel.setFont(Font.font(9));
        typeLabel.setTextAlignment(TextAlignment.CENTER);

        nameLabel = new Label(device.getName());
        nameLabel.setTextFill(Color.web("#EAEAEA"));
        nameLabel.setFont(Font.font(11));
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(NODE_WIDTH - 8);

        javafx.scene.layout.VBox labels = new javafx.scene.layout.VBox(2, typeLabel, nameLabel);
        labels.setAlignment(javafx.geometry.Pos.CENTER);

        getChildren().addAll(background, labels);
        setLayoutX(device.getX());
        setLayoutY(device.getY());

        setPrefSize(NODE_WIDTH, NODE_HEIGHT);
        setMaxSize(NODE_WIDTH, NODE_HEIGHT);
    }

    /** Returns the underlying Device model object. */
    public Device getDevice() { return device; }

    /** Marks this node as selected or deselected, updating visual style. */
    public void setSelected(boolean selected) {
        this.selected = selected;
        background.setFill(selected ? COLOR_SELECTED : COLOR_DEFAULT);
    }

    /** Returns true if this node is currently selected. */
    public boolean isSelected() { return selected; }

    /** Syncs this node's layout position to the device model's x/y. */
    public void syncPosition() {
        setLayoutX(device.getX());
        setLayoutY(device.getY());
    }

    /** Updates the name label to reflect current device name. */
    public void refreshName() {
        nameLabel.setText(device.getName());
    }
}
package com.filium.ui.panels;

import com.filium.model.devices.Device;
import com.filium.model.network.IPAddress;
import com.filium.model.network.NetworkInterface;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Right-hand panel that displays and allows inline editing of the
 * currently selected device's properties (name, IP, subnet mask, gateway).
 * Changes are committed on focus-lost / Enter.
 */
public class PropertiesPanel {

    private final VBox root;
    private final Label heading;
    private final Label typeLabel;
    private final TextField nameField;
    private final TextField ipField;
    private final TextField maskField;
    private final TextField gatewayField;
    private final Label macLabel;

    private Device currentDevice;
    private java.util.function.Consumer<Device> onDeviceChanged;

    public PropertiesPanel() {
        heading = new Label("Properties");
        heading.setFont(Font.font(null, FontWeight.BOLD, 13));
        heading.setTextFill(Color.web("#EAEAEA"));

        typeLabel = new Label("");
        typeLabel.setTextFill(Color.web("#A0A0B0"));
        typeLabel.setFont(Font.font(10));

        nameField    = editableField();
        ipField      = editableField();
        maskField    = editableField();
        gatewayField = editableField();
        macLabel     = new Label("");
        macLabel.setTextFill(Color.web("#888899"));
        macLabel.setFont(Font.font(10));

        GridPane grid = new GridPane();
        grid.setHgap(8); grid.setVgap(8);
        grid.setPadding(new Insets(8, 0, 8, 0));

        addRow(grid, 0, "Name",    nameField);
        addRow(grid, 1, "IP",      ipField);
        addRow(grid, 2, "Mask",    maskField);
        addRow(grid, 3, "Gateway", gatewayField);
        addRow(grid, 4, "MAC",     macLabel);

        root = new VBox(8, heading, typeLabel, new Separator(), grid);
        root.setPadding(new Insets(14, 12, 14, 12));
        root.setPrefWidth(190);
        root.setStyle("-fx-background-color: #1A1A2E; -fx-border-color: #2E2E4E;"
                    + " -fx-border-width: 0 0 0 1;");

        showDevice(null);

        // Commit changes on action/focus loss
        nameField.setOnAction(e    -> commitName());
        nameField.focusedProperty().addListener((o, ov, nv) -> { if (!nv) commitName(); });
        ipField.setOnAction(e      -> commitIP());
        ipField.focusedProperty().addListener((o, ov, nv)   -> { if (!nv) commitIP(); });
        maskField.setOnAction(e    -> commitMask());
        maskField.focusedProperty().addListener((o, ov, nv) -> { if (!nv) commitMask(); });
        gatewayField.setOnAction(e -> commitGateway());
        gatewayField.focusedProperty().addListener((o, ov, nv) -> { if (!nv) commitGateway(); });
    }

    public Node getNode() { return root; }

    /** Called whenever a device property is committed; use to refresh the canvas node. */
    public void setOnDeviceChanged(java.util.function.Consumer<Device> handler) {
        this.onDeviceChanged = handler;
    }

    /** Populates the panel with the given device's data, or clears if null. */
    public void showDevice(Device device) {
        this.currentDevice = device;
        if (device == null) {
            heading.setText("Properties");
            typeLabel.setText("No device selected");
            nameField.setText(""); nameField.setDisable(true);
            ipField.setText("");   ipField.setDisable(true);
            maskField.setText(""); maskField.setDisable(true);
            gatewayField.setText(""); gatewayField.setDisable(true);
            macLabel.setText("");
            return;
        }

        NetworkInterface ni = device.getNetworkInterface();
        heading.setText(device.getName());
        typeLabel.setText(device.getDeviceType().getDisplayName());
        nameField.setText(device.getName()); nameField.setDisable(false);
        ipField.setText(ni.getIpAddress() != null ? ni.getIpAddress().getAddress() : "");
        ipField.setDisable(false);
        maskField.setText(ni.getSubnetMask() != null ? ni.getSubnetMask() : "");
        maskField.setDisable(false);
        gatewayField.setText(ni.getDefaultGateway() != null
            ? ni.getDefaultGateway().getAddress() : "");
        gatewayField.setDisable(false);
        macLabel.setText(ni.getMacAddress());
    }

    // ─────────────────────────────────────────────────────────────────

    private void commitName() {
        if (currentDevice == null) return;
        String v = nameField.getText().trim();
        if (!v.isEmpty()) {
            currentDevice.setName(v);
            heading.setText(v);
            if (onDeviceChanged != null) onDeviceChanged.accept(currentDevice);
        }
    }

    private void commitIP() {
        if (currentDevice == null) return;
        String v = ipField.getText().trim();
        try {
            currentDevice.getNetworkInterface().setIpAddress(
                v.isEmpty() ? null : new IPAddress(v));
        } catch (IllegalArgumentException ignored) {
            // Restore previous value
            IPAddress prev = currentDevice.getNetworkInterface().getIpAddress();
            ipField.setText(prev != null ? prev.getAddress() : "");
        }
    }

    private void commitMask() {
        if (currentDevice == null) return;
        String v = maskField.getText().trim();
        try {
            if (!v.isEmpty()) currentDevice.getNetworkInterface().setSubnetMask(v);
        } catch (IllegalArgumentException ignored) {
            maskField.setText(currentDevice.getNetworkInterface().getSubnetMask());
        }
    }

    private void commitGateway() {
        if (currentDevice == null) return;
        String v = gatewayField.getText().trim();
        try {
            currentDevice.getNetworkInterface().setDefaultGateway(
                v.isEmpty() ? null : new IPAddress(v));
        } catch (IllegalArgumentException ignored) {
            IPAddress prev = currentDevice.getNetworkInterface().getDefaultGateway();
            gatewayField.setText(prev != null ? prev.getAddress() : "");
        }
    }

    private TextField editableField() {
        TextField tf = new TextField();
        tf.setStyle("-fx-background-color: #0F1535; -fx-text-fill: #EAEAEA;"
                  + " -fx-border-color: #2E2E4E; -fx-border-radius: 4;"
                  + " -fx-background-radius: 4; -fx-font-size: 11;");
        tf.setPrefWidth(130);
        return tf;
    }

    private void addRow(GridPane grid, int row, String labelText, Node field) {
        Label lbl = new Label(labelText);
        lbl.setTextFill(Color.web("#888899"));
        lbl.setFont(Font.font(10));
        lbl.setAlignment(Pos.CENTER_RIGHT);
        lbl.setMinWidth(55);
        grid.add(lbl, 0, row);
        grid.add(field, 1, row);
    }
}
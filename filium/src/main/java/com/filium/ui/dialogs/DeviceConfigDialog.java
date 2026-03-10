package com.filium.ui.dialogs;

import com.filium.model.devices.Device;
import com.filium.model.devices.DHCPServer;
import com.filium.model.devices.DNSServer;
import com.filium.model.network.IPAddress;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.StageStyle;

/**
 * Modal dialog for configuring a device: name, IP address, subnet mask,
 * default gateway, and type-specific settings (DHCP pool, DNS records).
 * Returns the modified Device on OK, empty on Cancel.
 */
public class DeviceConfigDialog extends Dialog<Device> {

    private final Device device;

    private final TextField nameField    = field();
    private final TextField ipField      = field();
    private final TextField maskField    = field();
    private final TextField gatewayField = field();

    // DHCP-specific
    private final TextField poolStartField = field();
    private final TextField poolEndField   = field();

    // DNS-specific
    private final TextField dnsHostField  = field();
    private final TextField dnsIPField    = field();

    public DeviceConfigDialog(Device device) {
        this.device = device;

        initStyle(StageStyle.UTILITY);
        setTitle("Configure — " + device.getName());
        setHeaderText(device.getDeviceType().getDisplayName()
            + ":  " + device.getName());
        setResizable(true);

        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setStyle("-fx-background-color: #1A1A2E;");

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(16));

        // Pre-populate
        nameField.setText(device.getName());
        IPAddress ip = device.getNetworkInterface().getIpAddress();
        ipField.setText(ip != null ? ip.getAddress() : "");
        maskField.setText(device.getNetworkInterface().getSubnetMask() != null
            ? device.getNetworkInterface().getSubnetMask() : "255.255.255.0");
        IPAddress gw = device.getNetworkInterface().getDefaultGateway();
        gatewayField.setText(gw != null ? gw.getAddress() : "");

        addRow(grid, 0, "Name",           nameField);
        addRow(grid, 1, "IP Address",     ipField);
        addRow(grid, 2, "Subnet Mask",    maskField);
        addRow(grid, 3, "Default Gateway", gatewayField);

        int row = 4;

        if (device instanceof DHCPServer dhcp) {
            addSeparatorRow(grid, row++, "DHCP Pool");
            IPAddress ps = dhcp.getPoolStart();
            IPAddress pe = dhcp.getPoolEnd();
            poolStartField.setText(ps != null ? ps.getAddress() : "");
            poolEndField.setText(pe != null ? pe.getAddress() : "");
            addRow(grid, row++, "Pool Start", poolStartField);
            addRow(grid, row++, "Pool End",   poolEndField);
        }

        if (device instanceof DNSServer dns) {
            addSeparatorRow(grid, row++, "Add DNS Record");
            dnsHostField.setPromptText("hostname");
            dnsIPField.setPromptText("ip address");
            addRow(grid, row++, "Hostname", dnsHostField);
            addRow(grid, row,   "IP",       dnsIPField);
        }

        getDialogPane().setContent(grid);

        setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            applyChanges();
            return device;
        });
    }

    // ─────────────────────────────────────────────────────────────────

    private void applyChanges() {
        String name = nameField.getText().trim();
        if (!name.isEmpty()) device.setName(name);

        try {
            String ipStr = ipField.getText().trim();
            device.getNetworkInterface().setIpAddress(
                ipStr.isEmpty() ? null : new IPAddress(ipStr));
        } catch (IllegalArgumentException ignored) {}

        try {
            String mask = maskField.getText().trim();
            if (!mask.isEmpty()) device.getNetworkInterface().setSubnetMask(mask);
        } catch (IllegalArgumentException ignored) {}

        try {
            String gw = gatewayField.getText().trim();
            device.getNetworkInterface().setDefaultGateway(
                gw.isEmpty() ? null : new IPAddress(gw));
        } catch (IllegalArgumentException ignored) {}

        if (device instanceof DHCPServer dhcp) {
            try {
                String ps = poolStartField.getText().trim();
                String pe = poolEndField.getText().trim();
                if (!ps.isEmpty() && !pe.isEmpty()) {
                    dhcp.setPool(new IPAddress(ps), new IPAddress(pe));
                }
            } catch (IllegalArgumentException ignored) {}
        }

        if (device instanceof DNSServer dns) {
            try {
                String host = dnsHostField.getText().trim();
                String dip  = dnsIPField.getText().trim();
                if (!host.isEmpty() && !dip.isEmpty()) {
                    dns.addRecord(host, new IPAddress(dip));
                }
            } catch (IllegalArgumentException ignored) {}
        }
    }

    private TextField field() {
        TextField tf = new TextField();
        tf.setStyle("-fx-background-color: #0F1535; -fx-text-fill: #EAEAEA;"
                  + " -fx-border-color: #2E2E4E; -fx-border-radius: 4;"
                  + " -fx-background-radius: 4; -fx-font-size: 12;");
        tf.setPrefWidth(200);
        return tf;
    }

    private void addRow(GridPane grid, int row, String labelText, javafx.scene.Node field) {
        Label lbl = new Label(labelText);
        lbl.setTextFill(Color.web("#A0A0B0"));
        lbl.setFont(Font.font(11));
        lbl.setMinWidth(110);
        grid.add(lbl, 0, row);
        grid.add(field, 1, row);
    }

    private void addSeparatorRow(GridPane grid, int row, String title) {
        Label lbl = new Label("── " + title + " ──");
        lbl.setTextFill(Color.web("#4A4A7A"));
        lbl.setFont(Font.font(10));
        grid.add(lbl, 0, row, 2, 1);
    }
}
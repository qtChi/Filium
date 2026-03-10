package com.filium.ui.panels;

import com.filium.model.devices.DeviceType;
import com.filium.model.network.NetworkTopology;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;

import java.util.function.BiConsumer;

/**
 * Left sidebar containing the device palette.
 * Each device type is shown as a draggable tile.
 * Notifies the CanvasController via onAddDevice callback.
 */
public class SidebarPanel {

    private final NetworkTopology topology;
    private final VBox root;

    private BiConsumer<DeviceType, double[]> onAddDevice;

    public SidebarPanel(NetworkTopology topology) {
        this.topology = topology;

        Label heading = new Label("DEVICES");
        heading.setTextFill(Color.web("#A0A0B0"));
        heading.setFont(Font.font(10));
        heading.setPadding(new Insets(0, 0, 4, 0));

        root = new VBox(8);
        root.setPadding(new Insets(14, 10, 14, 10));
        root.setPrefWidth(120);
        root.setStyle("-fx-background-color: #1A1A2E; -fx-border-color: #2E2E4E;"
                    + " -fx-border-width: 0 1 0 0;");

        root.getChildren().add(heading);
        root.getChildren().add(new Separator());

        for (DeviceType type : DeviceType.values()) {
            root.getChildren().add(buildTile(type));
        }
    }

    public Node getNode() { return root; }

    /** Callback fired when user clicks a tile; args are (DeviceType, [x, y]). */
    public void setOnAddDevice(BiConsumer<DeviceType, double[]> handler) {
        this.onAddDevice = handler;
    }

    // ─────────────────────────────────────────────────────────────────

    private VBox buildTile(DeviceType type) {
        Rectangle icon = new Rectangle(42, 42);
        icon.setArcWidth(8); icon.setArcHeight(8);
        icon.setFill(Color.web(tileColour(type)));
        icon.setStroke(Color.web("#00D4FF")); icon.setStrokeWidth(1);

        Label lbl = new Label(type.getDisplayName());
        lbl.setTextFill(Color.web("#CCCCDD"));
        lbl.setFont(Font.font(10));
        lbl.setWrapText(true);
        lbl.setMaxWidth(100);

        VBox tile = new VBox(4, icon, lbl);
        tile.setAlignment(Pos.CENTER);
        tile.setPadding(new Insets(6));
        tile.setStyle("-fx-background-color: #0F1535; -fx-background-radius: 6;"
                    + " -fx-cursor: hand;");

        Tooltip.install(tile, new Tooltip("Add " + type.getDisplayName()));

        tile.setOnMouseEntered(e ->
            tile.setStyle("-fx-background-color: #1E2050; -fx-background-radius: 6;"
                        + " -fx-cursor: hand;"));
        tile.setOnMouseExited(e ->
            tile.setStyle("-fx-background-color: #0F1535; -fx-background-radius: 6;"
                        + " -fx-cursor: hand;"));
        tile.setOnMouseClicked(e -> {
            if (onAddDevice != null) {
                // Place new device near the centre of the visible canvas
                onAddDevice.accept(type, new double[]{300 + Math.random() * 200,
                                                       200 + Math.random() * 200});
            }
        });

        return tile;
    }

    private String tileColour(DeviceType type) {
        return switch (type) {
            case PC          -> "#0F3460";
            case ROUTER      -> "#16213E";
            case SWITCH      -> "#1A1A2E";
            case DNS_SERVER  -> "#2C003E";
            case DHCP_SERVER -> "#003E2C";
            case FIREWALL    -> "#3E1A00";
        };
    }
}
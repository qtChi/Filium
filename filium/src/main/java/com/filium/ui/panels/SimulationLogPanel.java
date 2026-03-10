package com.filium.ui.panels;

import com.filium.simulation.SimulationEvent;
import com.filium.simulation.SimulationEventType;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Bottom log panel that displays SimulationEvents as they are emitted.
 * Thread-safe — appendEvent() may be called from any thread.
 */
public class SimulationLogPanel {

    private static final int MAX_LOG_ENTRIES = 500;
    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    private final BorderPane root;
    private final ListView<String> listView;

    public SimulationLogPanel() {

        listView = new ListView<>();
        listView.setPrefHeight(130);
        listView.setStyle("-fx-background-color: #0D0D1F; -fx-border-color: transparent;"
                        + " -fx-control-inner-background: #0D0D1F;"
                        + " -fx-font-family: monospace; -fx-font-size: 11;");
    
        Label heading = new Label("Simulation Log");
        heading.setFont(Font.font(null, FontWeight.BOLD, 11));
        heading.setTextFill(Color.web("#A0A0B0"));
        heading.setPadding(new Insets(4, 8, 4, 8));
    
        Button btnClear = new Button("Clear");
        btnClear.setStyle("-fx-background-color: #2E2E4E; -fx-text-fill: #CCCCDD;"
                        + " -fx-background-radius: 4; -fx-font-size: 10;");
        btnClear.setOnAction(e -> listView.getItems().clear());
    
        HBox header = new HBox(8, heading, btnClear);
        header.setPadding(new Insets(4, 8, 4, 8));
        header.setStyle("-fx-background-color: #12122A; -fx-border-color: #2E2E4E;"
                      + " -fx-border-width: 1 0 0 0;");
    
        root = new BorderPane();
        root.setTop(header);
        root.setCenter(listView);
        root.setStyle("-fx-background-color: #0D0D1F;");
    }

    public Node getNode() { return root; }

    /**
     * Appends a SimulationEvent to the log. Safe to call from any thread.
     */
    public void appendEvent(SimulationEvent event) {
        String entry = formatEntry(event);
        Platform.runLater(() -> {
            if (listView.getItems().size() >= MAX_LOG_ENTRIES) {
                listView.getItems().remove(0);
            }
            listView.getItems().add(entry);
            listView.scrollTo(listView.getItems().size() - 1);
        });
    }

    /** Clears all log entries. */
    public void clear() {
        Platform.runLater(() -> listView.getItems().clear());
    }

    // ─────────────────────────────────────────────────────────────────

    private String formatEntry(SimulationEvent event) {
        String time = TIME_FMT.format(Instant.ofEpochMilli(event.getTimestamp()));
        String tag  = tag(event.getType());
        String src  = event.getSource()      != null ? event.getSource().getName()      : "-";
        String dst  = event.getDestination() != null ? event.getDestination().getName() : "-";
        return String.format("[%s] %s  %s -> %s  |  %s",
            time, tag, src, dst, event.getMessage());
    }

    private String tag(SimulationEventType type) {
        return switch (type) {
            case PACKET_SENT     -> "[SENT    ]";
            case PACKET_RECEIVED -> "[RECV    ]";
            case PACKET_DROPPED  -> "[DROPPED ]";
            case PACKET_TTL_EXPIRED -> "[TTL_EXP ]";
            case DNS_RESOLVED    -> "[DNS_OK  ]";
            case DNS_FAILED      -> "[DNS_FAIL]";
            case DHCP_ASSIGNED   -> "[DHCP_OK ]";
            case ARP_RESOLVED    -> "[ARP_OK  ]";
            case FIREWALL_BLOCKED -> "[FW_BLOCK]";
            case FIREWALL_ALLOWED -> "[FW_ALLOW]";
        };
    }
}
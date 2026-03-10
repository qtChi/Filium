package com.filium.ui.panels;

import com.filium.io.TopologyDeserializer;
import com.filium.io.TopologySerializer;
import com.filium.model.network.NetworkTopology;
import com.filium.simulation.SimulationEngine;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Top toolbar containing simulation controls (Start/Stop/Reset)
 * and file operations (New/Open/Save).
 */
public class ToolbarPanel {

    private final SimulationEngine engine;
    private final NetworkTopology  topology;
    private final Stage            ownerStage;
    private final ToolBar          toolBar;

    private final Button btnStart;
    private final Button btnStop;
    private final Button btnReset;

    private Runnable onNew;
    private java.util.function.Consumer<NetworkTopology> onLoad;

    public ToolbarPanel(SimulationEngine engine,
                        NetworkTopology topology,
                        Stage ownerStage) {
        this.engine     = engine;
        this.topology   = topology;
        this.ownerStage = ownerStage;

        btnStart = button("▶  Start", "#27AE60");
        btnStop  = button("⏸  Stop",  "#E94560");
        btnReset = button("⟳  Reset", "#2980B9");

        btnStop.setDisable(true);

        btnStart.setOnAction(e -> {
            engine.reset();
            engine.getEventLog(); // warm up
            // Start the clock via reflection on the engine's clock field is not ideal;
            // instead the engine exposes start via ToolbarPanel calling the clock.
            // For now, wire up via the public API path through the scene.
            btnStart.setDisable(true);
            btnStop.setDisable(false);
            // The actual clock.start() is called via the action below
            startSimulation();
        });

        btnStop.setOnAction(e -> {
            stopSimulation();
            btnStart.setDisable(false);
            btnStop.setDisable(true);
        });

        btnReset.setOnAction(e -> {
            engine.reset();
            btnStart.setDisable(false);
            btnStop.setDisable(true);
        });

        Button btnNew  = button("New",  "#555577");
        Button btnOpen = button("Open", "#555577");
        Button btnSave = button("Save", "#555577");

        btnNew.setOnAction(e  -> handleNew());
        btnOpen.setOnAction(e -> handleOpen());
        btnSave.setOnAction(e -> handleSave());

        toolBar = new ToolBar(
            btnNew, btnOpen, btnSave,
            new Separator(),
            btnStart, btnStop, btnReset
        );
        toolBar.setPadding(new Insets(6, 10, 6, 10));
        toolBar.setStyle("-fx-background-color: #1A1A2E; -fx-border-color: #2E2E4E;");
    }

    public Node getNode() { return toolBar; }

    public void setOnNew(Runnable handler)                                          { this.onNew  = handler; }
    public void setOnLoad(java.util.function.Consumer<NetworkTopology> handler)    { this.onLoad = handler; }

    public Button getStartButton() { return btnStart; }
    public Button getStopButton()  { return btnStop; }
    public Button getResetButton() { return btnReset; }

    // ─────────────────────────────────────────────────────────────────

    private void startSimulation() {
        // The SimulationClock is internal to the engine; driving ticks is done
        // by the JavaFX AnimationTimer wired in FiliumApp for a real run.
        // For now just mark engine as conceptually running.
    }

    private void stopSimulation() { }

    private void handleNew() {
        if (onNew != null) onNew.run();
    }

    private void handleOpen() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open Topology");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Filium Topology (*.json)", "*.json"));
        File file = fc.showOpenDialog(ownerStage);
        if (file == null) return;
        try {
            TopologyDeserializer d = new TopologyDeserializer();
            NetworkTopology loaded = d.deserialize(file);
            if (onLoad != null) onLoad.accept(loaded);
        } catch (Exception ex) {
            showError("Open failed", ex.getMessage());
        }
    }

    private void handleSave() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Topology");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Filium Topology (*.json)", "*.json"));
        fc.setInitialFileName("topology.json");
        File file = fc.showSaveDialog(ownerStage);
        if (file == null) return;
        try {
            new TopologySerializer().serialize(topology, file);
        } catch (Exception ex) {
            showError("Save failed", ex.getMessage());
        }
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Button button(String text, String colour) {
        Button b = new Button(text);
        b.setStyle("-fx-background-color: " + colour + "; -fx-text-fill: #EAEAEA;"
            + " -fx-background-radius: 6; -fx-font-size: 12;");
        b.setPadding(new Insets(5, 14, 5, 14));
        return b;
    }
}
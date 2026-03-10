package com.filium.app;

import com.filium.model.network.NetworkTopology;
import com.filium.simulation.ARPTable;
import com.filium.simulation.SimulationClock;
import com.filium.simulation.SimulationEngine;
import com.filium.simulation.protocols.*;
import com.filium.ui.canvas.CanvasController;
import com.filium.ui.panels.PropertiesPanel;
import com.filium.ui.panels.SidebarPanel;
import com.filium.ui.panels.SimulationLogPanel;
import com.filium.ui.panels.ToolbarPanel;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * JavaFX Application root for Filium.
 * Wires together topology, simulation engine, UI panels, and the
 * AnimationTimer that drives the engine tick loop.
 */
public class FiliumApp extends Application {

    public static final String APP_TITLE  = "Filium — Network Simulator";
    public static final double MIN_WIDTH  = 1100;
    public static final double MIN_HEIGHT = 720;

    // Nanoseconds between ticks: ~20 ticks/sec at DEFAULT_SPEED 1.0
    private static final long TICK_INTERVAL_NS = 50_000_000L;

    private SimulationEngine engine;
    private SimulationClock  clock;
    private AnimationTimer   tickTimer;

    @Override
    public void start(Stage primaryStage) {
        // ── Model ────────────────────────────────────────────────────
        NetworkTopology topology = new NetworkTopology();

        ProtocolRegistry registry = new ProtocolRegistry();
        registry.register(new ICMPHandler());
        registry.register(new ARPHandler(new ARPTable()));
        registry.register(new DNSHandler());
        registry.register(new DHCPHandler());
        registry.register(new HTTPHandler());

        clock  = new SimulationClock();
        engine = new SimulationEngine(topology, registry, clock);

        // ── UI panels ────────────────────────────────────────────────
        ToolbarPanel      toolbar  = new ToolbarPanel(engine, topology, primaryStage);
        SidebarPanel      sidebar  = new SidebarPanel(topology);
        CanvasController  canvas   = new CanvasController(topology, engine);
        PropertiesPanel   props    = new PropertiesPanel();
        SimulationLogPanel logPanel = new SimulationLogPanel();

        // ── Wire up events ───────────────────────────────────────────
        engine.addListener(logPanel::appendEvent);
        canvas.setOnDeviceSelected(props::showDevice);
        props.setOnDeviceChanged(canvas::refreshDeviceNode);

        sidebar.setOnAddDevice((type, xy) ->
            canvas.addDevice(type, xy[0], xy[1]));

        toolbar.setOnCableModeChanged(canvas::setCableMode);

        toolbar.setOnNew(() -> {
            canvas.clear();
            engine.reset();
            logPanel.clear();
            props.showDevice(null);
        });

        toolbar.setOnLoad(loaded -> {
            topology.clear();
            loaded.getDevices().forEach(topology::addDevice);
            loaded.getCables().forEach(topology::addCable);
            canvas.reload();
            engine.reset();
            logPanel.clear();
            props.showDevice(null);
        });

        toolbar.getStartButton().setOnAction(e -> {
            clock.start();
            toolbar.getStartButton().setDisable(true);
            toolbar.getStopButton().setDisable(false);
        });

        toolbar.getStopButton().setOnAction(e -> {
            clock.stop();
            toolbar.getStartButton().setDisable(false);
            toolbar.getStopButton().setDisable(true);
        });

        toolbar.getResetButton().setOnAction(e -> {
            clock.stop();
            engine.reset();
            canvas.clear();
            logPanel.clear();
            props.showDevice(null);
            toolbar.getStartButton().setDisable(false);
            toolbar.getStopButton().setDisable(true);
        });

        // ── AnimationTimer tick loop ─────────────────────────────────
        tickTimer = new AnimationTimer() {
            private long lastTick = 0;
            @Override
            public void handle(long now) {
                long interval = (long) (TICK_INTERVAL_NS / clock.getSpeed());
                if (now - lastTick >= interval) {
                    engine.tick();
                    lastTick = now;
                }
            }
        };
        tickTimer.start();

        // ── Layout ───────────────────────────────────────────────────
        BorderPane root = new BorderPane();
        root.setTop(toolbar.getNode());
        root.setLeft(sidebar.getNode());
        root.setCenter(canvas.getNode());
        root.setRight(props.getNode());
        root.setBottom(logPanel.getNode());

        Scene scene = new Scene(root, MIN_WIDTH, MIN_HEIGHT);
        try {
            scene.getStylesheets().add(
                getClass().getResource("/css/dark-theme.css").toExternalForm());
        } catch (Exception ignored) {
            // CSS is optional
        }

        primaryStage.setTitle(APP_TITLE);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> tickTimer.stop());
        primaryStage.show();
    }
}
package com.filium.ui.canvas;

import com.filium.model.devices.Device;
import com.filium.model.devices.DeviceType;
import com.filium.model.devices.PC;
import com.filium.model.devices.Router;
import com.filium.model.devices.Switch;
import com.filium.model.devices.DNSServer;
import com.filium.model.devices.DHCPServer;
import com.filium.model.devices.Firewall;
import com.filium.model.network.Cable;
import com.filium.model.network.NetworkTopology;
import com.filium.simulation.SimulationEngine;
import com.filium.ui.dialogs.DeviceConfigDialog;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Manages the interactive network canvas.
 * Handles adding/removing devices, drawing cables, drag-to-move,
 * single-click selection, and double-click configuration.
 */
public class CanvasController {

    private final NetworkTopology topology;
    private final SimulationEngine engine;
    private final NetworkCanvas canvas;
    private final ScrollPane scrollPane;

    private final Map<Device, DeviceNode> deviceNodes = new HashMap<>();
    private final Map<Cable, CableEdge>   cableEdges  = new HashMap<>();

    private DeviceNode selectedNode = null;
    private DeviceNode cableSourceNode = null;
    private boolean cableMode = false;

    private Consumer<Device> onDeviceSelected;

    // Drag tracking
    private double dragStartX, dragStartY;

    public CanvasController(NetworkTopology topology, SimulationEngine engine) {
        this.topology = topology;
        this.engine   = engine;
        this.canvas   = new NetworkCanvas();

        scrollPane = new ScrollPane(canvas);
        scrollPane.setPannable(true);
        scrollPane.setFitToWidth(false);
        scrollPane.setFitToHeight(false);
        scrollPane.setStyle("-fx-background: #16213E; -fx-background-color: #16213E;");
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // Click on empty canvas deselects
        canvas.setOnMouseClicked(e -> {
            if (e.getTarget() == canvas) clearSelection();
        });
    }

    /** Returns the root node to embed in the scene. */
    public Node getNode() { return scrollPane; }

    /** Sets the callback invoked when a device node is selected. */
    public void setOnDeviceSelected(Consumer<Device> handler) {
        this.onDeviceSelected = handler;
    }

    /** Enters or exits cable-draw mode. */
    public void setCableMode(boolean enabled) {
        cableMode = enabled;
        if (!enabled) cableSourceNode = null;
    }

    public boolean isCableMode() { return cableMode; }

    /**
     * Adds a new device of the given type at the specified canvas coordinates.
     */
    public void addDevice(DeviceType type, double x, double y) {
        Device device = createDevice(type, defaultName(type));
        device.setX(x);
        device.setY(y);
        topology.addDevice(device);
        addDeviceNode(device);
    }

    /**
     * Removes the given device and all connected cables from the canvas and topology.
     */
    public void removeDevice(Device device) {
        topology.getCablesForDevice(device).forEach(cable -> {
            CableEdge edge = cableEdges.remove(cable);
            if (edge != null) canvas.getChildren().remove(edge);
        });
        topology.removeDevice(device);
        DeviceNode node = deviceNodes.remove(device);
        if (node != null) canvas.getChildren().remove(node);
        if (selectedNode != null && selectedNode.getDevice() == device) {
            selectedNode = null;
        }
    }

    /** Clears the canvas and topology completely. */
    public void clear() {
        canvas.getChildren().clear();
        deviceNodes.clear();
        cableEdges.clear();
        topology.clear();
        selectedNode = null;
        cableSourceNode = null;
    }

    /** Rebuilds the canvas from the current topology state (after load). */
    public void reload() {
        canvas.getChildren().clear();
        deviceNodes.clear();
        cableEdges.clear();
        selectedNode = null;

        for (Device d : topology.getDevices()) {
            addDeviceNode(d);
        }
        for (Cable c : topology.getCables()) {
            DeviceNode a = deviceNodes.get(c.getEndpointA());
            DeviceNode b = deviceNodes.get(c.getEndpointB());
            if (a != null && b != null) addCableEdge(c, a, b);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Internal helpers
    // ─────────────────────────────────────────────────────────────────

    private void addDeviceNode(Device device) {
        DeviceNode node = new DeviceNode(device);

        node.setOnMouseClicked(e -> {
            e.consume();
            if (cableMode) {
                handleCableModeClick(node);
            } else {
                if (e.getClickCount() == 2) {
                    openConfigDialog(device);
                } else {
                    selectNode(node);
                }
            }
        });

        node.setOnMousePressed(e -> {
            dragStartX = e.getSceneX() - node.getLayoutX();
            dragStartY = e.getSceneY() - node.getLayoutY();
        });

        node.setOnMouseDragged(e -> {
            if (!cableMode) {
                double nx = e.getSceneX() - dragStartX;
                double ny = e.getSceneY() - dragStartY;
                node.setLayoutX(nx);
                node.setLayoutY(ny);
                device.setX(nx);
                device.setY(ny);
            }
        });

        deviceNodes.put(device, node);
        canvas.getChildren().add(node);
    }

    private void addCableEdge(Cable cable, DeviceNode a, DeviceNode b) {
        CableEdge edge = new CableEdge(cable, a, b);
        cableEdges.put(cable, edge);
        // Add edges behind nodes
        canvas.getChildren().add(0, edge);
    }

    private void handleCableModeClick(DeviceNode node) {
        if (cableSourceNode == null) {
            cableSourceNode = node;
            node.setSelected(true);
        } else if (cableSourceNode == node) {
            cableSourceNode.setSelected(false);
            cableSourceNode = null;
        } else {
            Cable cable = new Cable(cableSourceNode.getDevice(), node.getDevice());
            topology.addCable(cable);
            addCableEdge(cable, cableSourceNode, node);
            cableSourceNode.setSelected(false);
            cableSourceNode = null;
        }
    }

    private void selectNode(DeviceNode node) {
        clearSelection();
        selectedNode = node;
        node.setSelected(true);
        if (onDeviceSelected != null) {
            onDeviceSelected.accept(node.getDevice());
        }
    }

    private void clearSelection() {
        if (selectedNode != null) {
            selectedNode.setSelected(false);
            selectedNode = null;
        }
        if (onDeviceSelected != null) {
            onDeviceSelected.accept(null);
        }
    }

    private void openConfigDialog(Device device) {
        DeviceConfigDialog dialog = new DeviceConfigDialog(device);
        dialog.showAndWait().ifPresent(updated -> {
            DeviceNode node = deviceNodes.get(device);
            if (node != null) node.refreshName();
        });
    }

    private Device createDevice(DeviceType type, String name) {
        return switch (type) {
            case PC          -> new PC(name);
            case ROUTER      -> new Router(name);
            case SWITCH      -> new Switch(name);
            case DNS_SERVER  -> new DNSServer(name);
            case DHCP_SERVER -> new DHCPServer(name);
            case FIREWALL    -> new Firewall(name);
        };
    }

    private String defaultName(DeviceType type) {
        long count = topology.getDevices().stream()
            .filter(d -> d.getDeviceType() == type).count();
        return type.getDisplayName() + "-" + (count + 1);
    }
}
package com.filium.ui.dialogs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.StageStyle;

/**
 * Simple About dialog showing version and attribution information.
 */
public class AboutDialog extends Dialog<Void> {

    public AboutDialog() {
        initStyle(StageStyle.UTILITY);
        setTitle("About Filium");
        setHeaderText(null);
        getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        getDialogPane().setStyle("-fx-background-color: #1A1A2E;");

        Label title = new Label("Filium");
        title.setFont(Font.font(null, FontWeight.BOLD, 22));
        title.setTextFill(Color.web("#00D4FF"));

        Label subtitle = new Label("Network Simulation Tool");
        subtitle.setTextFill(Color.web("#A0A0B0"));
        subtitle.setFont(Font.font(13));

        Label version = new Label("Version 1.0.0");
        version.setTextFill(Color.web("#666688"));
        version.setFont(Font.font(11));

        Label stack = new Label("Java 21  ·  JavaFX 21  ·  Jackson 2.x");
        stack.setTextFill(Color.web("#555577"));
        stack.setFont(Font.font(10));

        Label desc = new Label(
            "Filium lets you design network topologies,\n"
            + "configure devices, and watch packets flow\n"
            + "through your network in real time.");
        desc.setTextFill(Color.web("#CCCCDD"));
        desc.setFont(Font.font(12));
        desc.setWrapText(true);

        VBox content = new VBox(10, title, subtitle, version, stack,
            new javafx.scene.control.Separator(), desc);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(20, 30, 10, 30));
        content.setPrefWidth(320);

        getDialogPane().setContent(content);
        setResultConverter(b -> null);
    }
}
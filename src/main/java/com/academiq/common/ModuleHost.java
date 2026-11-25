package com.academiq.common;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.*;

/**
 * Main application window: navigation sidebar on the left, the active screen on the right.
 * Screens are created by reflection from the class names in ModuleRegistry.
 */
public class ModuleHost {
    private static Label statusBar;
    private final Stage stage;
    private final StackPane centerPane = new StackPane();
    private final List<ModulePanel> modules = new ArrayList<>();
    private final Map<String, Button> navButtons = new LinkedHashMap<>();
    private final Runnable onLogout;

    private static final String NAV_BTN_STYLE =
        "-fx-background-color: transparent; " +
        "-fx-text-fill: #283593; " +
        "-fx-font-size: 13px; " +
        "-fx-font-weight: bold; " +
        "-fx-alignment: CENTER_LEFT; " +
        "-fx-padding: 10 14; " +
        "-fx-background-radius: 6; " +
        "-fx-cursor: hand;";

    private static final String NAV_ACTIVE_STYLE =
        "-fx-background-color: #9FA8DA; " +
        "-fx-text-fill: #1A237E; " +
        "-fx-font-size: 13px; " +
        "-fx-font-weight: bold; " +
        "-fx-alignment: CENTER_LEFT; " +
        "-fx-padding: 10 14; " +
        "-fx-background-radius: 6; " +
        "-fx-cursor: hand;";

    public ModuleHost(String title, List<String> classNames, Runnable onLogout) {
        this(new Stage(), title, classNames, onLogout);
    }

    public ModuleHost(Stage stage, String title, List<String> classNames, Runnable onLogout) {
        this.stage = stage;
        this.onLogout = onLogout;
        Dialogs.setOwner(stage);
        stage.setTitle(title);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + Ui.HEX_BG + ";");

        // header
        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(12, 18, 12, 18));
        top.setStyle("-fx-background-color: " + Ui.HEX_PRIMARY + ";");

        Label brand = new Label("  AcademiQ  |  " + Session.name());
        brand.setFont(Font.font("System", FontWeight.BOLD, 17));
        brand.setTextFill(Color.WHITE);
        HBox.setHgrow(brand, Priority.ALWAYS);
        brand.setMaxWidth(Double.MAX_VALUE);

        top.getChildren().add(brand);

        if (onLogout != null) {
            Button logoutBtn = new Button("Logout");
            logoutBtn.setStyle(
                "-fx-background-color: transparent; " +
                "-fx-border-color: white; " +
                "-fx-border-radius: 4; " +
                "-fx-text-fill: white; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 4 12; " +
                "-fx-cursor: hand;"
            );
            logoutBtn.setOnAction(e -> logout());
            top.getChildren().add(logoutBtn);
        }
        root.setTop(top);

        // sidebar
        VBox nav = new VBox(4);
        nav.setPadding(new Insets(10, 8, 10, 8));
        nav.setPrefWidth(200);
        nav.setStyle("-fx-background-color: #E8EAF6; -fx-border-color: transparent #D1D5DB transparent transparent;");

        root.setLeft(nav);
        root.setCenter(centerPane);

        // status bar
        statusBar = new Label(" Ready");
        statusBar.setMaxWidth(Double.MAX_VALUE);
        statusBar.setFont(Font.font("System", 12));
        statusBar.setPadding(new Insets(5, 10, 5, 10));
        statusBar.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #424242;");
        root.setBottom(statusBar);

        // load screens
        for (String cn : classNames) {
            try {
                Object o = Class.forName(cn).getDeclaredConstructor().newInstance();
                if (o instanceof ModulePanel) {
                    ModulePanel mp = (ModulePanel) o;
                    modules.add(mp);
                    Node node = mp.getNode();
                    node.setVisible(false);
                    node.setManaged(false);
                    centerPane.getChildren().add(node);

                    Button b = new Button(mp.getTitle());
                    b.setMaxWidth(Double.MAX_VALUE);
                    b.setStyle(NAV_BTN_STYLE);
                    b.setOnAction(e -> show(mp.getTitle()));
                    navButtons.put(mp.getTitle(), b);
                    nav.getChildren().add(b);
                }
            } catch (ClassNotFoundException e) {
                // screen class not available, skip it
            } catch (Exception e) {
                System.err.println("Could not load module " + cn + ": " + e);
                e.printStackTrace();
            }
        }

        stage.setOnCloseRequest(e -> {
            shutdown();
            Platform.exit();
            System.exit(0);
        });

        Scene scene = new Scene(root, 1100, 720);
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
    }

    public void open() {
        open(this.stage);
    }

    public void open(Stage targetStage) {
        targetStage.show();
        for (ModulePanel m : modules) m.start();
        if (!modules.isEmpty()) {
            show(modules.get(0).getTitle());
        }
    }

    private void show(String title) {
        for (ModulePanel m : modules) {
            boolean active = m.getTitle().equals(title);
            Node node = m.getNode();
            node.setVisible(active);
            node.setManaged(active);

            Button b = navButtons.get(m.getTitle());
            if (b != null) {
                b.setStyle(active ? NAV_ACTIVE_STYLE : NAV_BTN_STYLE);
            }
            if (active) {
                m.onShow();
            }
        }
    }

    private void shutdown() {
        for (ModulePanel m : modules) m.stop();
    }

    private void logout() {
        shutdown();
        Session.logout();
        stage.close();
        if (onLogout != null) onLogout.run();
    }

    /** Thread-safe: may be called from background threads. */
    public static void setStatus(String msg) {
        Platform.runLater(() -> {
            if (statusBar != null) statusBar.setText(" " + msg);
        });
    }
}

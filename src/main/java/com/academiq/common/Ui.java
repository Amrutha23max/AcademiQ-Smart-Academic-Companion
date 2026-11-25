package com.academiq.common;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/** Factory methods and styling shared by all screens. */
public final class Ui {
    public static final String HEX_PRIMARY = "#3F51B5";
    public static final String HEX_PRIMARY_DARK = "#283593";
    public static final String HEX_BG = "#F5F6FA";
    public static final String HEX_CARD_BG = "#FFFFFF";
    public static final String HEX_DANGER = "#C62828";
    public static final String HEX_OK = "#2E7D32";
    public static final String HEX_BORDER = "#D1D5DB";

    public static final Color PRIMARY = Color.web(HEX_PRIMARY);
    public static final Color PRIMARY_DARK = Color.web(HEX_PRIMARY_DARK);
    public static final Color BG = Color.web(HEX_BG);
    public static final Color DANGER = Color.web(HEX_DANGER);
    public static final Color OK = Color.web(HEX_OK);

    public static final Font BASE = Font.font("System", 13);
    public static final Font BOLD = Font.font("System", FontWeight.BOLD, 13);
    public static final Font TITLE = Font.font("System", FontWeight.BOLD, 20);
    public static final Font MONO = Font.font("Consolas", 12);

    private static final String BTN_STYLE =
        "-fx-background-color: " + HEX_PRIMARY + "; " +
        "-fx-text-fill: white; " +
        "-fx-font-weight: bold; " +
        "-fx-font-size: 13px; " +
        "-fx-padding: 6 14; " +
        "-fx-background-radius: 5; " +
        "-fx-cursor: hand;";

    private static final String FIELD_STYLE =
        "-fx-background-color: white; " +
        "-fx-border-color: " + HEX_BORDER + "; " +
        "-fx-border-radius: 4; " +
        "-fx-background-radius: 4; " +
        "-fx-padding: 5 8; " +
        "-fx-font-size: 13px;";

    private Ui() {}

    public static Label heading(String text) {
        Label l = new Label(text);
        l.setFont(TITLE);
        l.setStyle("-fx-text-fill: " + HEX_PRIMARY_DARK + "; -fx-font-weight: bold; -fx-padding: 0 0 10 0;");
        return l;
    }

    public static Label label(String text) {
        Label l = new Label(text);
        l.setFont(BASE);
        l.setStyle("-fx-text-fill: #333333;");
        return l;
    }

    public static Label boldLabel(String text) {
        Label l = new Label(text);
        l.setFont(BOLD);
        l.setStyle("-fx-text-fill: #222222; -fx-font-weight: bold;");
        return l;
    }

    public static TextField field(int cols) {
        TextField t = new TextField();
        t.setPrefColumnCount(cols);
        t.setFont(BASE);
        t.setStyle(FIELD_STYLE);
        return t;
    }

    public static PasswordField password(int cols) {
        PasswordField t = new PasswordField();
        t.setPrefColumnCount(cols);
        t.setFont(BASE);
        t.setStyle(FIELD_STYLE);
        return t;
    }

    public static Button button(String text, EventHandler<ActionEvent> l) {
        Button b = new Button(text);
        b.setStyle(BTN_STYLE);
        b.setOnMouseEntered(e -> b.setStyle(BTN_STYLE + "-fx-background-color: " + HEX_PRIMARY_DARK + ";"));
        b.setOnMouseExited(e -> b.setStyle(BTN_STYLE));
        if (l != null) b.setOnAction(l);
        return b;
    }

    /** Left-aligned horizontal row of components. */
    public static HBox row(Node... nodes) {
        HBox box = new HBox(8, nodes);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(6, 0, 6, 0));
        return box;
    }

    /** Creates an empty GridPane form panel; fill it with addRow. */
    public static GridPane form() {
        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(8);
        g.setPadding(new Insets(6, 0, 8, 0));
        return g;
    }

    public static void addRow(GridPane form, int rowIndex, String labelText, Node input) {
        Label lbl = label(labelText);
        lbl.setMinWidth(120);
        form.add(lbl, 0, rowIndex);
        form.add(input, 1, rowIndex);
        GridPane.setHgrow(input, Priority.ALWAYS);
    }

    /** Shows an error from a caught exception. */
    public static void showError(Exception e) {
        Dialogs.error(e.getMessage());
    }
}

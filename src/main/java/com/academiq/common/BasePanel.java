package com.academiq.common;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

/** Base class for every screen: background, padding and default lifecycle methods. */
public abstract class BasePanel extends BorderPane implements ModulePanel {
    protected BasePanel() {
        setStyle("-fx-background-color: " + Ui.HEX_BG + ";");
        setPadding(new Insets(14, 18, 14, 18));
    }

    @Override
    public void onShow() {}

    @Override
    public Node getNode() {
        return this;
    }

    protected void status(String message) {
        ModuleHost.setStatus(message);
    }
}

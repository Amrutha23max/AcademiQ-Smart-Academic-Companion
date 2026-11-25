package com.academiq.common;

import javafx.scene.Node;

/** Contract for a screen shown in the main window. Implementations are JavaFX nodes (usually extending BasePanel). */
public interface ModulePanel {
    String getTitle();

    /** Called every time the screen is shown: reload data from the database here. */
    void onShow();

    /** Called once after login (start schedulers here). */
    default void start() {}

    /** Called when the window closes (shut down schedulers here). */
    default void stop() {}

    /** The node to display for this screen. */
    default Node getNode() {
        if (this instanceof Node) return (Node) this;
        throw new UnsupportedOperationException("ModulePanel must be a JavaFX Node or implement getNode()");
    }
}

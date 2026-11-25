package com.academiq.common;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/** Alert dialogs and file choosers that can be called from any thread. */
public final class Dialogs {
    public static final int LOAD = 0;
    public static final int SAVE = 1;

    private static Stage ownerStage;
    private Dialogs() {}

    public static void setOwner(Stage stage) { ownerStage = stage; }
    public static Window owner() { return ownerStage; }

    public static void info(String msg) {
        runOnFx(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            if (ownerStage != null && ownerStage.isShowing()) alert.initOwner(ownerStage);
            alert.setTitle("AcademiQ");
            alert.setHeaderText(null);
            alert.setContentText(msg != null ? msg : "");
            alert.showAndWait();
            return null;
        });
    }

    public static void error(String msg) {
        runOnFx(() -> {
            Alert alert = new Alert(AlertType.ERROR);
            if (ownerStage != null && ownerStage.isShowing()) alert.initOwner(ownerStage);
            alert.setTitle("Error");
            alert.setHeaderText(null);
            alert.setContentText(msg != null ? msg : "");
            alert.showAndWait();
            return null;
        });
    }

    public static boolean confirm(String msg) {
        Boolean res = runOnFx(() -> {
            Alert alert = new Alert(AlertType.CONFIRMATION, msg != null ? msg : "", ButtonType.YES, ButtonType.NO);
            if (ownerStage != null && ownerStage.isShowing()) alert.initOwner(ownerStage);
            alert.setTitle("Please confirm");
            alert.setHeaderText(null);
            Optional<ButtonType> opt = alert.showAndWait();
            return opt.isPresent() && opt.get() == ButtonType.YES;
        });
        return res != null && res;
    }

    /** Shows a file chooser. mode is LOAD (0) or SAVE (1). Returns null if cancelled. */
    public static File chooseFile(String title, int mode, String defaultName) {
        return runOnFx(() -> {
            FileChooser fc = new FileChooser();
            fc.setTitle(title != null ? title : "Select File");
            if (defaultName != null && !defaultName.trim().isEmpty()) {
                fc.setInitialFileName(defaultName.trim());
            }
            if (mode == SAVE) {
                return fc.showSaveDialog(ownerStage);
            } else {
                return fc.showOpenDialog(ownerStage);
            }
        });
    }

    private static <T> T runOnFx(Callable<T> action) {
        if (Platform.isFxApplicationThread()) {
            try {
                return action.call();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        FutureTask<T> task = new FutureTask<>(action);
        Platform.runLater(task);
        try {
            return task.get();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

package com.academiq;

import com.academiq.common.*;
import com.academiq.account.ui.LoginFrame;
import javafx.application.Application;
import javafx.stage.Stage;

/** Entry point: installs the database tables, shows the login window, then opens the main window. */
public class Main {
    public static void main(String[] args) {
        try {
            SchemaInstaller.install();
        } catch (DatabaseException e) {
            System.err.println(e.getMessage());
            Dialogs.error(e.getMessage());
            System.exit(1);
        }
        Application.launch(MainApp.class, args);
    }

    public static class MainApp extends Application {
        @Override
        public void start(Stage primaryStage) {
            showLogin();
        }

        public static void showLogin() {
            new LoginFrame(MainApp::openMainWindow).setVisible(true);
        }

        public static void openMainWindow() {
            ModuleHost host = new ModuleHost("AcademiQ - Smart Academic Companion", ModuleRegistry.SCREENS, MainApp::showLogin);
            host.open();
        }
    }
}

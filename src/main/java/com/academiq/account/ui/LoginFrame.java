package com.academiq.account.ui;

import com.academiq.common.*;
import com.academiq.account.service.AuthService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/** Login and registration window. */
public class LoginFrame extends Stage {
    private final AuthService auth = new AuthService();
    private final Runnable onSuccess;

    private final TextField loginEmail = Ui.field(24);
    private final PasswordField loginPwd = Ui.password(24);

    private final TextField regName = Ui.field(24);
    private final TextField regEmail = Ui.field(24);
    private final TextField regCourse = Ui.field(24);
    private final PasswordField regPwd = Ui.password(24);
    private final PasswordField regConfirm = Ui.password(24);

    private final StackPane deck = new StackPane();
    private final VBox loginView;
    private final VBox registerView;

    public LoginFrame(Runnable onSuccess) {
        this.onSuccess = onSuccess;
        Dialogs.setOwner(this);
        setTitle("AcademiQ - Sign in");
        setResizable(true);
        setMinWidth(480);
        setMinHeight(520);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + Ui.HEX_BG + ";");

        // header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(16));
        header.setStyle("-fx-background-color: " + Ui.HEX_PRIMARY + ";");
        Label t = new Label("ACADEMIQ - Smart Academic Companion");
        t.setTextFill(Color.WHITE);
        t.setFont(Font.font("System", FontWeight.BOLD, 17));
        header.getChildren().add(t);
        root.setTop(header);

        loginView = buildLogin();
        registerView = buildRegister();
        registerView.setVisible(false);
        registerView.setManaged(false);

        deck.getChildren().addAll(loginView, registerView);
        root.setCenter(deck);

        setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        Scene scene = new Scene(root, 480, 520);
        setScene(scene);
        centerOnScreen();
    }

    private VBox buildLogin() {
        VBox box = new VBox(16);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(24, 30, 24, 30));

        Label heading = Ui.heading("Welcome back");

        GridPane form = Ui.form();
        form.setAlignment(Pos.CENTER);
        Ui.addRow(form, 0, "Email", loginEmail);
        Ui.addRow(form, 1, "Password", loginPwd);
        loginPwd.setOnAction(e -> doLogin());

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER);
        Button loginBtn = Ui.button("Login", e -> doLogin());
        Button createAccBtn = Ui.button("Create account", e -> showRegister());
        createAccBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-border-color: " + Ui.HEX_PRIMARY + "; " +
            "-fx-border-radius: 4; " +
            "-fx-text-fill: " + Ui.HEX_PRIMARY + "; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 6 14; " +
            "-fx-cursor: hand;"
        );
        buttons.getChildren().addAll(loginBtn, createAccBtn);

        box.getChildren().addAll(heading, form, buttons);
        return box;
    }

    private VBox buildRegister() {
        VBox box = new VBox(12);
        box.setAlignment(Pos.TOP_CENTER);
        box.setPadding(new Insets(18, 30, 18, 30));

        Label heading = Ui.heading("Create your account");

        GridPane form = Ui.form();
        form.setAlignment(Pos.CENTER);
        Ui.addRow(form, 0, "Full name", regName);
        Ui.addRow(form, 1, "Email", regEmail);
        Ui.addRow(form, 2, "Course / Branch", regCourse);
        Ui.addRow(form, 3, "Password", regPwd);
        Ui.addRow(form, 4, "Confirm password", regConfirm);

        HBox buttons = new HBox(12);
        buttons.setAlignment(Pos.CENTER);
        Button regBtn = Ui.button("Register", e -> doRegister());
        Button backBtn = Ui.button("Back to login", e -> showLogin());
        backBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-border-color: #757575; " +
            "-fx-border-radius: 4; " +
            "-fx-text-fill: #424242; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 6 14; " +
            "-fx-cursor: hand;"
        );
        buttons.getChildren().addAll(regBtn, backBtn);

        box.getChildren().addAll(heading, form, buttons);
        return box;
    }

    private void showLogin() {
        registerView.setVisible(false);
        registerView.setManaged(false);
        loginView.setVisible(true);
        loginView.setManaged(true);
    }

    private void showRegister() {
        loginView.setVisible(false);
        loginView.setManaged(false);
        registerView.setVisible(true);
        registerView.setManaged(true);
    }

    private void doLogin() {
        try {
            auth.login(loginEmail.getText(), loginPwd.getText());
            close();
            if (onSuccess != null) onSuccess.run();
        } catch (AcademiqException ex) {
            Dialogs.error(ex.getMessage());
        }
    }

    private void doRegister() {
        try {
            auth.register(regName.getText(), regEmail.getText(), regCourse.getText(), regPwd.getText(), regConfirm.getText());
            Dialogs.info("Account created. You can log in now.");
            loginEmail.setText(regEmail.getText());
            regPwd.setText("");
            regConfirm.setText("");
            showLogin();
        } catch (AcademiqException ex) {
            Dialogs.error(ex.getMessage());
        }
    }

    public void setVisible(boolean visible) {
        if (visible) show();
        else hide();
    }

    public void dispose() {
        close();
    }
}

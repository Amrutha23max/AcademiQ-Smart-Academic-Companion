package com.academiq.account.ui;

import com.academiq.common.*;
import com.academiq.account.model.User;
import com.academiq.account.service.AuthService;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/** Screen "Profile": view/edit profile and change password. */
public class ProfilePanel extends BasePanel {
    private final AuthService auth = new AuthService();
    private final Label email = Ui.label("");
    private final TextField name = Ui.field(26), course = Ui.field(26);
    private final PasswordField oldPwd = Ui.password(20), newPwd = Ui.password(20), confirm = Ui.password(20);

    public ProfilePanel() {
        VBox top = new VBox(10);
        top.getChildren().add(Ui.heading("My Profile"));

        VBox body = new VBox(20);

        // profile details
        VBox p1 = new VBox(8);
        p1.setPadding(new Insets(14));
        p1.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-radius: 6; -fx-background-radius: 6;");
        p1.getChildren().add(Ui.boldLabel("Profile details"));
        GridPane f1 = Ui.form();
        Ui.addRow(f1, 0, "Email (cannot change)", email);
        Ui.addRow(f1, 1, "Full name", name);
        Ui.addRow(f1, 2, "Course / Branch", course);
        p1.getChildren().addAll(f1, Ui.row(Ui.button("Save profile", e -> saveProfile())));

        // change password
        VBox p2 = new VBox(8);
        p2.setPadding(new Insets(14));
        p2.setStyle("-fx-background-color: white; -fx-border-color: #E0E0E0; -fx-border-radius: 6; -fx-background-radius: 6;");
        p2.getChildren().add(Ui.boldLabel("Change password"));
        GridPane f2 = Ui.form();
        Ui.addRow(f2, 0, "Current password", oldPwd);
        Ui.addRow(f2, 1, "New password", newPwd);
        Ui.addRow(f2, 2, "Confirm new password", confirm);
        p2.getChildren().addAll(f2, Ui.row(Ui.button("Change password", e -> changePassword())));

        body.getChildren().addAll(p1, p2);
        top.getChildren().add(body);
        setTop(top);
    }

    @Override
    public String getTitle() { return "Profile"; }

    @Override
    public void onShow() {
        try {
            User u = auth.currentUser();
            if (u != null) {
                email.setText(u.getEmail());
                name.setText(u.getFullName());
                course.setText(u.getCourse() == null ? "" : u.getCourse());
            }
        } catch (DatabaseException e) {
            Ui.showError(e);
        }
    }

    private void saveProfile() {
        try {
            auth.updateProfile(name.getText(), course.getText());
            Dialogs.info("Profile saved.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void changePassword() {
        try {
            auth.changePassword(oldPwd.getText(), newPwd.getText(), confirm.getText());
            oldPwd.setText("");
            newPwd.setText("");
            confirm.setText("");
            Dialogs.info("Password changed.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }
}

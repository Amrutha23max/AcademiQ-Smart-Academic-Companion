package com.academiq.common;

import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import java.util.ArrayList;
import java.util.List;

/** A ComboBox filled with the current student's subjects. */
public class SubjectChoice extends ComboBox<String> {
    private final List<Subject> subjects = new ArrayList<>();
    private final boolean withAllOption;

    public SubjectChoice(boolean withAllOption) {
        this.withAllOption = withAllOption;
        setPrefWidth(220);
        setStyle("-fx-font-size: 13px; -fx-background-color: white; -fx-border-color: #D1D5DB; -fx-border-radius: 4;");
    }

    public void reload() {
        String previous = getValue();
        subjects.clear();
        List<String> names = new ArrayList<>();
        if (withAllOption) names.add("All subjects");
        try {
            for (Subject s : new SubjectDAO().findAll()) {
                subjects.add(s);
                names.add(s.getName());
            }
        } catch (DatabaseException e) {
            Dialogs.error(e.getMessage());
        }
        setItems(FXCollections.observableArrayList(names));
        if (previous != null && names.contains(previous)) {
            setValue(previous);
        } else if (!names.isEmpty()) {
            getSelectionModel().selectFirst();
        }
    }

    /** Selected subject id; -1 when "All subjects" (or nothing) is chosen. */
    public int selectedSubjectId() {
        int i = getSelectionModel().getSelectedIndex() - (withAllOption ? 1 : 0);
        return (i < 0 || i >= subjects.size()) ? -1 : subjects.get(i).getId();
    }

    public void selectId(int subjectId) {
        for (int i = 0; i < subjects.size(); i++) {
            if (subjects.get(i).getId() == subjectId) {
                getSelectionModel().select(i + (withAllOption ? 1 : 0));
                return;
            }
        }
    }
}

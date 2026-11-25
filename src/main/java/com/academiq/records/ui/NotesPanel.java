package com.academiq.records.ui;

import com.academiq.common.*;
import com.academiq.records.model.Note;
import com.academiq.records.service.NoteService;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Screen "Notes": attach files to subjects, search, open, delete. */
public class NotesPanel extends BasePanel {
    private final NoteService service = new NoteService();
    private final SubjectChoice subject = new SubjectChoice(false);
    private final SubjectChoice filterSubject = new SubjectChoice(true);
    private final TextField title = Ui.field(28), topic = Ui.field(20), path = Ui.field(40), search = Ui.field(16);
    private final TableList table = new TableList(12, new String[]{"Title", "Subject", "Topic", "Date", "File"}, new int[]{24, 18, 16, 11, 30});

    public NotesPanel() {
        VBox top = new VBox(6);
        top.getChildren().add(Ui.heading("Notes & Study Materials"));

        GridPane form = Ui.form();
        Ui.addRow(form, 0, "Subject", subject);
        Ui.addRow(form, 1, "Title", title);
        Ui.addRow(form, 2, "Topic", topic);

        HBox fileBox = Ui.row(path, Ui.button("Browse...", e -> browse()));
        Ui.addRow(form, 3, "File (PDF, doc...)", fileBox);
        top.getChildren().add(form);

        VBox south = new VBox(6);
        south.getChildren().add(Ui.row(Ui.button("Add note", e -> add())));
        south.getChildren().add(Ui.row(
            Ui.boldLabel("Find:"), filterSubject, search,
            Ui.button("Search", e -> reload()),
            Ui.button("Open selected", e -> open()),
            Ui.button("Delete selected", e -> delete())
        ));
        top.getChildren().add(south);
        top.setPadding(new Insets(0, 0, 10, 0));

        setTop(top);
        setCenter(table);
        table.addDoubleClickListener(e -> open());
    }

    @Override
    public String getTitle() { return "Notes"; }

    @Override
    public void onShow() {
        subject.reload();
        filterSubject.reload();
        reload();
    }

    private void browse() {
        File f = Dialogs.chooseFile("Choose a study file", Dialogs.LOAD, null);
        if (f != null) {
            path.setText(f.getAbsolutePath());
            if (title.getText().isEmpty()) title.setText(f.getName());
        }
    }

    private void reload() {
        try {
            List<Note> notes = service.list(filterSubject.selectedSubjectId(), search.getText());
            List<Object[]> rows = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            for (Note n : notes) {
                rows.add(new Object[]{
                    n.getTitle(),
                    n.getSubjectName(),
                    n.getTopic(),
                    n.getUploadDate(),
                    new File(n.getFilePath()).getName()
                });
                ids.add(n.getId());
            }
            table.setRows(rows, ids);
        } catch (DatabaseException e) {
            Ui.showError(e);
        }
    }

    private void add() {
        try {
            service.add(subject.selectedSubjectId(), title.getText(), topic.getText(), path.getText());
            title.setText("");
            topic.setText("");
            path.setText("");
            reload();
            status("Note added.");
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void open() {
        try {
            service.open(Validators.requireSelection("note", table.getSelectedId()));
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }

    private void delete() {
        try {
            int id = Validators.requireSelection("note", table.getSelectedId());
            if (Dialogs.confirm("Delete this note and its stored file?")) {
                service.delete(id);
                reload();
                status("Note deleted.");
            }
        } catch (AcademiqException e) {
            Ui.showError(e);
        }
    }
}

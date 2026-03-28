package controller;

import dao.AuditDAO;
import dao.NoteDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.AuditNote;
import model.Note;

import java.util.List;

public class NoteController {

    @FXML private TextField txtNumEtudiant, txtNumMat, txtNote;
    @FXML private TableView<Note> tableNote;
    @FXML private TableColumn<Note, Integer> colEtudiant, colMat;
    @FXML private TableColumn<Note, Float>   colNote;
    @FXML private Label lblMessage;

    @FXML
    public void initialize() {
        colEtudiant.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getNumEtudiant()).asObject());
        colMat.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getNumMat()).asObject());
        colNote.setCellValueFactory(c -> new javafx.beans.property.SimpleFloatProperty(c.getValue().getNote()).asObject());

        tableNote.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                txtNumEtudiant.setText(String.valueOf(sel.getNumEtudiant()));
                txtNumMat.setText(String.valueOf(sel.getNumMat()));
                txtNote.setText(String.valueOf(sel.getNote()));
            }
        });

        loadData();
    }

    private void loadData() {
        tableNote.setItems(FXCollections.observableArrayList(NoteDAO.getAll()));
    }

    private boolean valider() {
        if (txtNumEtudiant.getText().isEmpty() || txtNumMat.getText().isEmpty() || txtNote.getText().isEmpty()) {
            showMsg("❌ Remplissez tous les champs !", "error");
            return false;
        }
        try {
            float note = Float.parseFloat(txtNote.getText());
            if (note < 0 || note > 20) {
                showMsg("❌ La note doit être entre 0 et 20 !", "error");
                return false;
            }
        } catch (NumberFormatException e) {
            showMsg("❌ Note invalide !", "error");
            return false;
        }
        return true;
    }

    @FXML
    private void handleAdd() {
        if (!valider()) return;
        try {
            Note n = new Note(
                    Integer.parseInt(txtNumEtudiant.getText()),
                    Integer.parseInt(txtNumMat.getText()),
                    Float.parseFloat(txtNote.getText())
            );
            if (NoteDAO.insertNote(n)) {
                showMsg("✅ Note ajoutée ! (Moyenne recalculée automatiquement)", "success");
                clearFields();
                loadData();
            } else {
                showMsg("❌ Erreur : vérifiez que l'étudiant et la matière existent.", "error");
            }
        } catch (NumberFormatException e) {
            showMsg("❌ Entrez des nombres valides !", "error");
        }
    }

    @FXML
    private void handleUpdate() {
        if (!valider()) return;
        try {
            Note n = new Note(
                    Integer.parseInt(txtNumEtudiant.getText()),
                    Integer.parseInt(txtNumMat.getText()),
                    Float.parseFloat(txtNote.getText())
            );
            if (NoteDAO.updateNote(n)) {
                showMsg("✅ Note modifiée ! (Moyenne recalculée automatiquement)", "success");
                clearFields();
                loadData();
            } else {
                showMsg("❌ Note introuvable pour cet étudiant/matière.", "error");
            }
        } catch (NumberFormatException e) {
            showMsg("❌ Entrez des nombres valides !", "error");
        }
    }

    @FXML
    private void handleDelete() {
        if (txtNumEtudiant.getText().isEmpty() || txtNumMat.getText().isEmpty()) {
            showMsg("❌ Entrez N° Étudiant et N° Matière !", "error");
            return;
        }
        try {
            Note n = new Note(
                    Integer.parseInt(txtNumEtudiant.getText()),
                    Integer.parseInt(txtNumMat.getText()),
                    0
            );
            if (NoteDAO.deleteNote(n)) {
                showMsg("✅ Note supprimée ! (Moyenne recalculée automatiquement)", "success");
                clearFields();
                loadData();
            } else {
                showMsg("❌ Note introuvable.", "error");
            }
        } catch (NumberFormatException e) {
            showMsg("❌ Entrez des nombres valides !", "error");
        }
    }

    @FXML
    private void handleClear() {
        clearFields();
        showMsg("", "");
    }

    private void clearFields() {
        txtNumEtudiant.clear();
        txtNumMat.clear();
        txtNote.clear();
        tableNote.getSelectionModel().clearSelection();
    }

    private void showMsg(String msg, String type) {
        lblMessage.setText(msg);
        lblMessage.getStyleClass().removeAll("success", "error");
        if (!type.isEmpty()) lblMessage.getStyleClass().add(type);
    }
}

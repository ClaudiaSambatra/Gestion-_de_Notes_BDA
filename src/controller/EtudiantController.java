package controller;

import dao.EtudiantDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Etudiant;

import java.util.List;

public class EtudiantController {

    @FXML private TextField txtNum, txtNom;
    @FXML private TableView<Etudiant> tableEtudiant;
    @FXML private TableColumn<Etudiant, Integer> colNum;
    @FXML private TableColumn<Etudiant, String>  colNom;
    @FXML private TableColumn<Etudiant, Float>   colMoyenne;
    @FXML private Label lblMessage;

    @FXML
    public void initialize() {
        colNum.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getNumEtudiant()).asObject());
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));
        colMoyenne.setCellValueFactory(c -> new javafx.beans.property.SimpleFloatProperty(c.getValue().getMoyenne()).asObject());

        // Clic sur ligne → remplir les champs
        tableEtudiant.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                txtNum.setText(String.valueOf(sel.getNumEtudiant()));
                txtNom.setText(sel.getNom());
                txtNum.setDisable(true); // ne pas changer la clé primaire
            }
        });

        loadData();
    }

    private void loadData() {
        List<Etudiant> list = EtudiantDAO.getAll();
        tableEtudiant.setItems(FXCollections.observableArrayList(list));
    }

    private boolean valider() {
        if (txtNum.getText().isEmpty() || txtNom.getText().isEmpty()) {
            showMsg("❌ Remplissez tous les champs !", "error");
            return false;
        }
        return true;
    }

    @FXML
    private void handleAdd() {
        if (!valider()) return;
        try {
            Etudiant e = new Etudiant(Integer.parseInt(txtNum.getText()), txtNom.getText(), 0);
            if (EtudiantDAO.insert(e)) {
                showMsg("✅ Étudiant ajouté !", "success");
                clearFields();
                loadData();
            } else {
                showMsg("❌ Erreur lors de l'ajout.", "error");
            }
        } catch (NumberFormatException ex) {
            showMsg("❌ Le numéro doit être un entier.", "error");
        }
    }

    @FXML
    private void handleUpdate() {
        if (!valider()) return;
        try {
            Etudiant e = new Etudiant(Integer.parseInt(txtNum.getText()), txtNom.getText(), 0);
            if (EtudiantDAO.update(e)) {
                showMsg("✅ Étudiant modifié !", "success");
                clearFields();
                loadData();
            } else {
                showMsg("❌ Erreur lors de la modification.", "error");
            }
        } catch (NumberFormatException ex) {
            showMsg("❌ Le numéro doit être un entier.", "error");
        }
    }

    @FXML
    private void handleDelete() {
        if (txtNum.getText().isEmpty()) {
            showMsg("❌ Entrez le N° Étudiant à supprimer.", "error");
            return;
        }
        try {
            if (EtudiantDAO.delete(Integer.parseInt(txtNum.getText()))) {
                showMsg("✅ Étudiant supprimé !", "success");
                clearFields();
                loadData();
            } else {
                showMsg("❌ Erreur lors de la suppression.", "error");
            }
        } catch (NumberFormatException ex) {
            showMsg("❌ Le numéro doit être un entier.", "error");
        }
    }

    @FXML
    private void handleRefresh() {
        clearFields();
        loadData();
        showMsg("✅ Liste actualisée !", "success");
    }
    private void clearFields() {
        txtNum.clear();
        txtNom.clear();
        txtNum.setDisable(false);
        tableEtudiant.getSelectionModel().clearSelection();
    }

    private void showMsg(String msg, String type) {
        lblMessage.setText(msg);
        lblMessage.getStyleClass().removeAll("success", "error");
        if (!type.isEmpty()) lblMessage.getStyleClass().add(type);
    }
}

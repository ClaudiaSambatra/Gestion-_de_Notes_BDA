package controller;

import dao.MatiereDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.Matiere;

import java.util.List;

public class MatiereController {

    @FXML private TextField txtNum, txtDesign, txtCoef;
    @FXML private TableView<Matiere> tableMatiere;
    @FXML private TableColumn<Matiere, Integer> colNum;
    @FXML private TableColumn<Matiere, String>  colDesign;
    @FXML private TableColumn<Matiere, Float>   colCoef;
    @FXML private Label lblMessage;

    @FXML
    public void initialize() {
        colNum.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().getNumMat()).asObject());
        colDesign.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDesign()));
        colCoef.setCellValueFactory(c -> new javafx.beans.property.SimpleFloatProperty(c.getValue().getCoef()).asObject());

        tableMatiere.getSelectionModel().selectedItemProperty().addListener((obs, old, sel) -> {
            if (sel != null) {
                txtNum.setText(String.valueOf(sel.getNumMat()));
                txtDesign.setText(sel.getDesign());
                txtCoef.setText(String.valueOf(sel.getCoef()));
                txtNum.setDisable(true);
            }
        });

        loadData();
    }

    private void loadData() {
        List<Matiere> list = MatiereDAO.getAll();
        tableMatiere.setItems(FXCollections.observableArrayList(list));
    }

    private boolean valider() {
        if (txtNum.getText().isEmpty() || txtDesign.getText().isEmpty() || txtCoef.getText().isEmpty()) {
            showMsg("❌ Remplissez tous les champs !", "error");
            return false;
        }
        return true;
    }

    @FXML
    private void handleAdd() {
        if (!valider()) return;
        try {
            Matiere m = new Matiere(Integer.parseInt(txtNum.getText()), txtDesign.getText(), Float.parseFloat(txtCoef.getText()));
            if (MatiereDAO.insert(m)) {
                showMsg("✅ Matière ajoutée !", "success");
                clearFields();
                loadData();
            } else {
                showMsg("❌ Erreur lors de l'ajout.", "error");
            }
        } catch (NumberFormatException ex) {
            showMsg("❌ Valeurs numériques invalides.", "error");
        }
    }

    @FXML
    private void handleUpdate() {
        if (!valider()) return;
        try {
            Matiere m = new Matiere(Integer.parseInt(txtNum.getText()), txtDesign.getText(), Float.parseFloat(txtCoef.getText()));
            if (MatiereDAO.update(m)) {
                showMsg("✅ Matière modifiée !", "success");
                clearFields();
                loadData();
            } else {
                showMsg("❌ Erreur lors de la modification.", "error");
            }
        } catch (NumberFormatException ex) {
            showMsg("❌ Valeurs numériques invalides.", "error");
        }
    }

    @FXML
    private void handleDelete() {
        if (txtNum.getText().isEmpty()) {
            showMsg("❌ Entrez le N° Matière à supprimer.", "error");
            return;
        }
        try {
            if (MatiereDAO.delete(Integer.parseInt(txtNum.getText()))) {
                showMsg("✅ Matière supprimée !", "success");
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
    private void handleClear() {
        clearFields();
        showMsg("", "");
    }

    private void clearFields() {
        txtNum.clear();
        txtDesign.clear();
        txtCoef.clear();
        txtNum.setDisable(false);
        tableMatiere.getSelectionModel().clearSelection();
    }

    private void showMsg(String msg, String type) {
        lblMessage.setText(msg);
        lblMessage.getStyleClass().removeAll("success", "error");
        if (!type.isEmpty()) lblMessage.getStyleClass().add(type);
    }
}

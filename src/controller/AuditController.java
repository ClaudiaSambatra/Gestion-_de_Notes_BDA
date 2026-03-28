package controller;

import dao.AuditDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.AuditNote;

import java.time.LocalDateTime;
import java.util.List;

public class AuditController {

    @FXML private TableView<AuditNote> tableAudit;
    @FXML private TableColumn<AuditNote, String>        colType, colEtudiant, colNom, colMatiere, colUtilisateur;
    @FXML private TableColumn<AuditNote, Float>         colAncienne, colNouvelle;
    @FXML private TableColumn<AuditNote, LocalDateTime> colDate;
    @FXML private Label lblInserts, lblUpdates, lblDeletes, lblTotal;
    @FXML private TextField txtFiltreType;

    private List<AuditNote> allAudits;

    @FXML
    public void initialize() {
        colType.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getTypeOperation()));
        colDate.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getDateOperation()));
        colEtudiant.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(String.valueOf(c.getValue().getNumEtudiant())));
        colNom.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNom()));
        colMatiere.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getDesign()));
        colAncienne.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getNoteAncien()));
        colNouvelle.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getNoteNouv()));
        colUtilisateur.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getUtilisateur()));

        // Couleur par type d'opération
        tableAudit.setRowFactory(tv -> new TableRow<AuditNote>() {
            @Override
            protected void updateItem(AuditNote item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-insert", "row-update", "row-delete");
                if (item != null) {
                    switch (item.getTypeOperation()) {
                        case "INSERT" -> getStyleClass().add("row-insert");
                        case "UPDATE" -> getStyleClass().add("row-update");
                        case "DELETE" -> getStyleClass().add("row-delete");
                    }
                }
            }
        });

        loadAudit();
    }

    private void loadAudit() {
        allAudits = AuditDAO.getAllAudit();
        afficherAudits(allAudits);
    }

    private void afficherAudits(List<AuditNote> audits) {
        tableAudit.setItems(FXCollections.observableArrayList(audits));
        long inserts = AuditDAO.countByType(audits, "INSERT");
        long updates = AuditDAO.countByType(audits, "UPDATE");
        long deletes = AuditDAO.countByType(audits, "DELETE");

        lblInserts.setText("Insertions : " + inserts);
        lblUpdates.setText("Modifications : " + updates);
        lblDeletes.setText("Suppressions : " + deletes);
        lblTotal.setText("Total : " + audits.size());
    }

    @FXML
    private void handleRefresh() {
        loadAudit();
    }

    @FXML
    private void handleFiltre() {
        String filtre = txtFiltreType.getText().toUpperCase().trim();
        if (filtre.isEmpty()) {
            afficherAudits(allAudits);
        } else {
            List<AuditNote> filtered = allAudits.stream()
                    .filter(a -> a.getTypeOperation().contains(filtre))
                    .toList();
            afficherAudits(filtered);
        }
    }

    @FXML
    private void handleShowAll()   { txtFiltreType.clear(); afficherAudits(allAudits); }
    @FXML
    private void handleShowInsert(){ txtFiltreType.setText("INSERT"); handleFiltre(); }
    @FXML
    private void handleShowUpdate(){ txtFiltreType.setText("UPDATE"); handleFiltre(); }
    @FXML
    private void handleShowDelete(){ txtFiltreType.setText("DELETE"); handleFiltre(); }
}

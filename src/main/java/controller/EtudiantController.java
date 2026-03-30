package controller;

import dao.EtudiantDAO;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import model.Etudiant;
import util.*;

import java.util.ArrayList;
import java.util.List;

public class EtudiantController implements Refreshable {
    @FXML
    private TextField txtSearch;
    @FXML
    private StackPane tablePane;
    @FXML
    private TableView<Etudiant> table;
    @FXML
    private TableColumn<Etudiant, Integer> colNum;
    @FXML
    private TableColumn<Etudiant, String> colNom;
    @FXML
    private TableColumn<Etudiant, Float> colMoyenne;
    @FXML
    private TableColumn<Etudiant, Void> colActions;
    @FXML
    private Label lblCount, lblPage;
    @FXML
    private Button btnPrev, btnNext;
    private List<Etudiant> allData = new ArrayList<>(), filtered = new ArrayList<>();
    private int page = 0;
    private static final int PS = 15;
    private Debouncer deb;

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colNum.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumEtudiant()).asObject());
        colNum.setMaxWidth(1f * Integer.MAX_VALUE * 12);
        colNum.setStyle("-fx-alignment:CENTER;");
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colNom.setMaxWidth(1f * Integer.MAX_VALUE * 48);
        colMoyenne.setCellValueFactory(c -> new SimpleFloatProperty(c.getValue().getMoyenne()).asObject());
        colMoyenne.setMaxWidth(1f * Integer.MAX_VALUE * 22);
        colMoyenne.setStyle("-fx-alignment:CENTER;");
        colMoyenne.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Float it, boolean em) {
                super.updateItem(it, em);
                setAlignment(Pos.CENTER);
                if (em || it == null) {
                    setText(null);
                    setStyle("-fx-alignment:CENTER;");
                    return;
                }
                setText(String.format("%.2f", it));
                String c = it < 10 ? "#ef4444" : it >= 14 ? "#22c55e" : "";
                setStyle("-fx-alignment:CENTER;" + (c.isEmpty() ? "" : "-fx-text-fill:" + c + ";-fx-font-weight:bold;"));
            }
        });
        colActions.setMaxWidth(1f * Integer.MAX_VALUE * 18);
        colActions.setStyle("-fx-alignment:CENTER;");
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button be = new Button("✎"), bd = new Button("✕");
            private final HBox bx = new HBox(6, be, bd);

            {
                be.getStyleClass().addAll("btn-icon", "btn-icon-edit");
                bd.getStyleClass().addAll("btn-icon", "btn-icon-delete");
                bx.setAlignment(Pos.CENTER);
                be.setOnAction(e -> editModal(getTableView().getItems().get(getIndex())));
                bd.setOnAction(e -> delModal(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void i, boolean em) {
                super.updateItem(i, em);
                setGraphic(em ? null : bx);
            }
        });
        deb = new Debouncer(Duration.millis(300), () -> {
            page = 0;
            filter();
        });
        txtSearch.textProperty().addListener((o, a, v) -> deb.trigger());
        refreshData();
    }

    @FXML
    private void handleAdd() {
        TextField n = new TextField(), nm = new TextField();
        n.setPromptText("ex:101");
        nm.setPromptText("Nom complet");
        var f = FxHelper.buildForm(FxHelper.labeledField("Numero", n), FxHelper.labeledField("Nom", nm));
        ModalHelper.show("Nouvel Etudiant", f, () -> {
            if (FxHelper.isValidInt(n.getText())) {
                Toast.error("Numero invalide.");
                return false;
            }
            if (nm.getText() == null || nm.getText().isBlank()) {
                Toast.error("Nom obligatoire.");
                return false;
            }
            int num = Integer.parseInt(n.getText().trim());
            if (EtudiantDAO.exists(num)) {
                Toast.error("Numero existant.");
                return false;
            }
            if (EtudiantDAO.insert(new Etudiant(num, nm.getText().trim(), 0))) {
                Toast.success("Etudiant ajoute.");
                refreshData();
                return true;
            }
            Toast.error("Erreur.");
            return false;
        });
    }

    private void editModal(Etudiant e) {
        TextField tf = new TextField(e.getNom());
        Label l = new Label("Etudiant #" + e.getNumEtudiant());
        l.getStyleClass().add("form-label");
        var f = FxHelper.buildForm(new javafx.scene.layout.VBox(4, l), FxHelper.labeledField("Nom", tf));
        ModalHelper.show("Modifier", f, () -> {
            if (tf.getText() == null || tf.getText().isBlank()) {
                Toast.error("Nom obligatoire.");
                return false;
            }
            if (EtudiantDAO.update(new Etudiant(e.getNumEtudiant(), tf.getText().trim(), 0))) {
                Toast.success("Modifie.");
                refreshData();
                return true;
            }
            Toast.error("Erreur.");
            return false;
        });
    }

    private void delModal(Etudiant e) {
        ModalHelper.confirm("Supprimer", "Supprimer \"" + e.getNom() + "\" ?", "Supprimer", "btn-modal-danger", () -> {
            if (EtudiantDAO.delete(e.getNumEtudiant())) {
                Toast.success("Supprime.");
                refreshData();
            } else Toast.error("Erreur.");
        });
    }

    @FXML
    private void handleRefresh() {
        refreshData();
        Toast.info("Actualise.");
    }

    @FXML
    private void handlePrev() {
        if (page > 0) {
            page--;
            upd();
        }
    }

    @FXML
    private void handleNext() {
        if ((page + 1) * PS < filtered.size()) {
            page++;
            upd();
        }
    }

    @Override
    public void refreshData() {
        DataService.loadAsync(EtudiantDAO::getAll, d -> {
            allData = d;
            page = 0;
            filter();
        }, tablePane);
    }

    private void filter() {
        String k = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        filtered = k.isEmpty() ? new ArrayList<>(allData) : allData.stream().filter(e -> String.valueOf(e.getNumEtudiant()).contains(k) || e.getNom().toLowerCase().contains(k) || String.format("%.2f", e.getMoyenne()).contains(k)).toList();
        upd();
    }

    private void upd() {
        int tp = Math.max(1, (int) Math.ceil((double) filtered.size() / PS));
        page = Math.min(page, tp - 1);
        int f = page * PS, t = Math.min(f + PS, filtered.size());
        table.setItems(FXCollections.observableArrayList(filtered.subList(f, t)));
        lblCount.setText(filtered.size() + " etudiant(s)");
        lblPage.setText("Page " + (page + 1) + " / " + tp);
        btnPrev.setDisable(page <= 0);
        btnNext.setDisable(page >= tp - 1);
    }
}

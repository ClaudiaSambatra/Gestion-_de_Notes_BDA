package controller;

import dao.EtudiantDAO;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
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

    private List<Etudiant> allData = new ArrayList<>();
    private List<Etudiant> filtered = new ArrayList<>();
    private int page = 0;
    private static final int PAGE_SIZE = 15;
    private Debouncer searchDebouncer;

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colNum.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumEtudiant()).asObject());
        colNum.setMaxWidth(1f * Integer.MAX_VALUE * 12);
        colNum.setStyle("-fx-alignment: CENTER;");

        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colNom.setMaxWidth(1f * Integer.MAX_VALUE * 48);

        colMoyenne.setCellValueFactory(c -> new SimpleFloatProperty(c.getValue().getMoyenne()).asObject());
        colMoyenne.setMaxWidth(1f * Integer.MAX_VALUE * 22);
        colMoyenne.setStyle("-fx-alignment: CENTER;");
        colMoyenne.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-alignment: CENTER;");
                    return;
                }
                setText(String.format("%.2f", item));
                String color = item < 10 ? "#ef4444" : item >= 14 ? "#22c55e" : "";
                setStyle("-fx-alignment: CENTER;" + (color.isEmpty() ? "" : "-fx-text-fill:" + color + ";-fx-font-weight:bold;"));
            }
        });

        colActions.setMaxWidth(1f * Integer.MAX_VALUE * 18);
        colActions.setStyle("-fx-alignment: CENTER;");
        setupActionColumn();

        searchDebouncer = new Debouncer(Duration.millis(300), () -> {
            page = 0;
            applyFilter();
        });
        txtSearch.textProperty().addListener((o, a, v) -> searchDebouncer.trigger());

        refreshData();
    }

    private void setupActionColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button be = new Button("\u270E");
            private final Button bd = new Button("\u2715");
            private final HBox box = new HBox(6, be, bd);

            {
                be.getStyleClass().addAll("btn-icon", "btn-icon-edit");
                bd.getStyleClass().addAll("btn-icon", "btn-icon-delete");
                box.setAlignment(Pos.CENTER);
                be.setOnAction(e -> openEdit(getTableView().getItems().get(getIndex())));
                bd.setOnAction(e -> confirmDel(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void i, boolean empty) {
                super.updateItem(i, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    @FXML
    private void handleAdd() {
        TextField tfN = new TextField();
        tfN.setPromptText("ex: 101");
        TextField tfNom = new TextField();
        tfNom.setPromptText("ex: Jean Dupont");
        var form = FxHelper.buildForm(FxHelper.labeledField("Numero", tfN), FxHelper.labeledField("Nom", tfNom));
        ModalHelper.show("Nouvel Etudiant", form, () -> {
            if (FxHelper.isValidInt(tfN.getText())) {
                Toast.error("Numero invalide.");
                return false;
            }
            if (tfNom.getText() == null || tfNom.getText().isBlank()) {
                Toast.error("Nom obligatoire.");
                return false;
            }
            int num = Integer.parseInt(tfN.getText().trim());
            if (EtudiantDAO.exists(num)) {
                Toast.error("Numero existant.");
                return false;
            }
            if (EtudiantDAO.insert(new Etudiant(num, tfNom.getText().trim(), 0))) {
                Toast.success("Etudiant ajoute.");
                refreshData();
                return true;
            }
            Toast.error("Erreur ajout.");
            return false;
        });
    }

    private void openEdit(Etudiant e) {
        TextField tf = new TextField(e.getNom());
        Label l = new Label("Etudiant #" + e.getNumEtudiant());
        l.getStyleClass().add("form-label");
        var form = FxHelper.buildForm(new javafx.scene.layout.VBox(4, l), FxHelper.labeledField("Nom", tf));
        ModalHelper.show("Modifier Etudiant", form, () -> {
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

    private void confirmDel(Etudiant e) {
        ModalHelper.confirm("Supprimer", "Supprimer \"" + e.getNom() + "\" et ses notes ?", "Supprimer", "btn-modal-danger", () -> {
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
            updateTable();
        }
    }

    @FXML
    private void handleNext() {
        if ((page + 1) * PAGE_SIZE < filtered.size()) {
            page++;
            updateTable();
        }
    }

    @Override
    public void refreshData() {
        DataService.loadAsync(EtudiantDAO::getAll, data -> {
            allData = data;
            page = 0;
            applyFilter();
        }, tablePane);
    }

    private void applyFilter() {
        String kw = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        filtered = kw.isEmpty() ? new ArrayList<>(allData) : allData.stream()
                                                             .filter(e -> String.valueOf(e.getNumEtudiant()).contains(kw)
                                                                          || e.getNom().toLowerCase().contains(kw)
                                                                          || String.format("%.2f", e.getMoyenne()).contains(kw)).toList();
        updateTable();
    }

    private void updateTable() {
        int tp = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));
        page = Math.min(page, tp - 1);
        int from = page * PAGE_SIZE, to = Math.min(from + PAGE_SIZE, filtered.size());
        table.setItems(FXCollections.observableArrayList(filtered.subList(from, to)));
        lblCount.setText(filtered.size() + " etudiant(s)");
        lblPage.setText("Page " + (page + 1) + " / " + tp);
        btnPrev.setDisable(page <= 0);
        btnNext.setDisable(page >= tp - 1);
    }
}

package controller;

import dao.MatiereDAO;
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
import model.Matiere;
import util.*;

import java.util.ArrayList;
import java.util.List;

public class MatiereController implements Refreshable {

    @FXML
    private TextField txtSearch;
    @FXML
    private StackPane tablePane;
    @FXML
    private TableView<Matiere> table;
    @FXML
    private TableColumn<Matiere, Integer> colNum;
    @FXML
    private TableColumn<Matiere, String> colDesign;
    @FXML
    private TableColumn<Matiere, Float> colCoef;
    @FXML
    private TableColumn<Matiere, Void> colActions;
    @FXML
    private Label lblCount, lblPage;
    @FXML
    private Button btnPrev, btnNext;

    private List<Matiere> allData = new ArrayList<>();
    private List<Matiere> filtered = new ArrayList<>();
    private int page = 0;
    private static final int PAGE_SIZE = 15;
    private Debouncer debouncer;

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colNum.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getNumMat()).asObject());
        colNum.setMaxWidth(1f * Integer.MAX_VALUE * 12);
        colNum.setStyle("-fx-alignment:CENTER;");
        colDesign.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDesign()));
        colDesign.setMaxWidth(1f * Integer.MAX_VALUE * 48);
        colCoef.setCellValueFactory(c -> new SimpleFloatProperty(c.getValue().getCoef()).asObject());
        colCoef.setMaxWidth(1f * Integer.MAX_VALUE * 22);
        colCoef.setStyle("-fx-alignment:CENTER;");
        colActions.setMaxWidth(1f * Integer.MAX_VALUE * 18);
        colActions.setStyle("-fx-alignment:CENTER;");
        setupActionColumn();
        debouncer = new Debouncer(Duration.millis(300), () -> {
            page = 0;
            applyFilter();
        });
        txtSearch.textProperty().addListener((o, a, v) -> debouncer.trigger());
        refreshData();
    }

    private void setupActionColumn() {
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button be = new Button("✎"), bd = new Button("✕");
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
        TextField tN = new TextField(), tD = new TextField(), tC = new TextField();
        tN.setPromptText("ex: 1");
        tD.setPromptText("ex: Maths");
        tC.setPromptText("ex: 3");
        var form = FxHelper.buildForm(FxHelper.labeledField("Numero", tN), FxHelper.labeledField("Designation", tD), FxHelper.labeledField("Coefficient", tC));
        ModalHelper.show("Nouvelle Matiere", form, () -> {
            if (FxHelper.isValidInt(tN.getText())) {
                Toast.error("Numero invalide.");
                return false;
            }
            if (tD.getText() == null || tD.getText().isBlank()) {
                Toast.error("Designation obligatoire.");
                return false;
            }
            if (FxHelper.isValidFloat(tC.getText())) {
                Toast.error("Coefficient invalide.");
                return false;
            }
            float c = Float.parseFloat(tC.getText().trim());
            if (c <= 0) {
                Toast.error("Coefficient positif.");
                return false;
            }
            if (MatiereDAO.insert(new Matiere(Integer.parseInt(tN.getText().trim()), tD.getText().trim(), c))) {
                Toast.success("Matiere ajoutee.");
                refreshData();
                return true;
            }
            Toast.error("Erreur.");
            return false;
        });
    }

    private void openEdit(Matiere m) {
        TextField tD = new TextField(m.getDesign()), tC = new TextField(FxHelper.fmt(m.getCoef()));
        Label l = new Label("Matiere #" + m.getNumMat());
        l.getStyleClass().add("form-label");
        var form = FxHelper.buildForm(new javafx.scene.layout.VBox(4, l), FxHelper.labeledField("Designation", tD), FxHelper.labeledField("Coefficient", tC));
        ModalHelper.show("Modifier Matiere", form, () -> {
            if (tD.getText() == null || tD.getText().isBlank()) {
                Toast.error("Designation obligatoire.");
                return false;
            }
            if (FxHelper.isValidFloat(tC.getText())) {
                Toast.error("Coefficient invalide.");
                return false;
            }
            if (MatiereDAO.update(new Matiere(m.getNumMat(), tD.getText().trim(), Float.parseFloat(tC.getText().trim())))) {
                Toast.success("Modifiee.");
                refreshData();
                return true;
            }
            Toast.error("Erreur.");
            return false;
        });
    }

    private void confirmDel(Matiere m) {
        ModalHelper.confirm("Supprimer", "Supprimer \"" + m.getDesign() + "\" et les notes ?", "Supprimer", "btn-modal-danger", () -> {
            if (MatiereDAO.delete(m.getNumMat())) {
                Toast.success("Supprimee.");
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
        DataService.loadAsync(MatiereDAO::getAll, d -> {
            allData = d;
            page = 0;
            applyFilter();
        }, tablePane);
    }

    private void applyFilter() {
        String kw = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        filtered = kw.isEmpty() ? new ArrayList<>(allData) : allData.stream()
                                                             .filter(m -> String.valueOf(m.getNumMat()).contains(kw) || m.getDesign().toLowerCase().contains(kw)).toList();
        updateTable();
    }

    private void updateTable() {
        int tp = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));
        page = Math.min(page, tp - 1);
        int f = page * PAGE_SIZE, t = Math.min(f + PAGE_SIZE, filtered.size());
        table.setItems(FXCollections.observableArrayList(filtered.subList(f, t)));
        lblCount.setText(filtered.size() + " matiere(s)");
        lblPage.setText("Page " + (page + 1) + " / " + tp);
        btnPrev.setDisable(page <= 0);
        btnNext.setDisable(page >= tp - 1);
    }
}

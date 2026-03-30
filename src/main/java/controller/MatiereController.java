package controller;

import dao.MatiereDAO;
import javafx.beans.property.*;
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
    private List<Matiere> allData = new ArrayList<>(), filtered = new ArrayList<>();
    private int page = 0;
    private static final int PS = 15;
    private Debouncer deb;

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
        TextField n = new TextField(), d = new TextField(), c = new TextField();
        n.setPromptText("ex:1");
        d.setPromptText("Maths");
        c.setPromptText("3");
        var f = FxHelper.buildForm(FxHelper.labeledField("Numero", n), FxHelper.labeledField("Designation", d), FxHelper.labeledField("Coefficient", c));
        ModalHelper.show("Nouvelle Matiere", f, () -> {
            if (FxHelper.isValidInt(n.getText())) {
                Toast.error("Numero invalide.");
                return false;
            }
            if (d.getText() == null || d.getText().isBlank()) {
                Toast.error("Designation obligatoire.");
                return false;
            }
            if (FxHelper.isValidFloat(c.getText())) {
                Toast.error("Coefficient invalide.");
                return false;
            }
            float co = Float.parseFloat(c.getText().trim());
            if (co <= 0) {
                Toast.error("Coefficient positif.");
                return false;
            }
            if (MatiereDAO.insert(new Matiere(Integer.parseInt(n.getText().trim()), d.getText().trim(), co))) {
                Toast.success("Matiere ajoutee.");
                refreshData();
                return true;
            }
            Toast.error("Erreur.");
            return false;
        });
    }

    private void editModal(Matiere m) {
        TextField d = new TextField(m.getDesign()), c = new TextField(FxHelper.fmt(m.getCoef()));
        Label l = new Label("Matiere #" + m.getNumMat());
        l.getStyleClass().add("form-label");
        var f = FxHelper.buildForm(new javafx.scene.layout.VBox(4, l), FxHelper.labeledField("Designation", d), FxHelper.labeledField("Coefficient", c));
        ModalHelper.show("Modifier", f, () -> {
            if (d.getText() == null || d.getText().isBlank()) {
                Toast.error("Designation obligatoire.");
                return false;
            }
            if (FxHelper.isValidFloat(c.getText())) {
                Toast.error("Coefficient invalide.");
                return false;
            }
            if (MatiereDAO.update(new Matiere(m.getNumMat(), d.getText().trim(), Float.parseFloat(c.getText().trim())))) {
                Toast.success("Modifiee.");
                refreshData();
                return true;
            }
            Toast.error("Erreur.");
            return false;
        });
    }

    private void delModal(Matiere m) {
        ModalHelper.confirm("Supprimer", "Supprimer \"" + m.getDesign() + "\" ?", "Supprimer", "btn-modal-danger", () -> {
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
        DataService.loadAsync(MatiereDAO::getAll, d -> {
            allData = d;
            page = 0;
            filter();
        }, tablePane);
    }

    private void filter() {
        String k = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        filtered = k.isEmpty() ? new ArrayList<>(allData) : allData.stream().filter(m -> String.valueOf(m.getNumMat()).contains(k) || m.getDesign().toLowerCase().contains(k)).toList();
        upd();
    }

    private void upd() {
        int tp = Math.max(1, (int) Math.ceil((double) filtered.size() / PS));
        page = Math.min(page, tp - 1);
        int f = page * PS, t = Math.min(f + PS, filtered.size());
        table.setItems(FXCollections.observableArrayList(filtered.subList(f, t)));
        lblCount.setText(filtered.size() + " matiere(s)");
        lblPage.setText("Page " + (page + 1) + " / " + tp);
        btnPrev.setDisable(page <= 0);
        btnNext.setDisable(page >= tp - 1);
    }
}

package controller;

import dao.EtudiantDAO;
import dao.MatiereDAO;
import dao.NoteDAO;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import model.*;
import util.*;

import java.util.ArrayList;
import java.util.List;

public class NoteController implements Refreshable {

    @FXML
    private TextField txtSearch;
    @FXML
    private StackPane tablePane;
    @FXML
    private TableView<NoteView> table;
    @FXML
    private TableColumn<NoteView, String> colEtudiant, colMatiere;
    @FXML
    private TableColumn<NoteView, Float> colNote, colCoef;
    @FXML
    private TableColumn<NoteView, Void> colActions;
    @FXML
    private Label lblCount, lblPage;
    @FXML
    private Button btnPrev, btnNext;

    private List<NoteView> allData = new ArrayList<>();
    private List<NoteView> filtered = new ArrayList<>();
    private int page = 0;
    private static final int PAGE_SIZE = 15;
    private Debouncer debouncer;

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colEtudiant.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNumEtudiant() + " - " + c.getValue().getNomEtudiant()));
        colEtudiant.setMaxWidth(1f * Integer.MAX_VALUE * 28);

        colMatiere.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDesignMatiere()));
        colMatiere.setMaxWidth(1f * Integer.MAX_VALUE * 26);

        colNote.setCellValueFactory(c -> new SimpleFloatProperty(c.getValue().getNote()).asObject());
        colNote.setMaxWidth(1f * Integer.MAX_VALUE * 15);
        colNote.setStyle("-fx-alignment: CENTER;");
        colNote.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER);
                if (empty || item == null) {
                    setText(null);
                    setStyle("-fx-alignment:CENTER;");
                    return;
                }
                setText(String.format("%.1f", item));
                String c = item < 10 ? "#ef4444" : item >= 16 ? "#22c55e" : "";
                setStyle("-fx-alignment:CENTER;" + (c.isEmpty() ? "" : "-fx-text-fill:" + c + ";-fx-font-weight:bold;"));
            }
        });

        colCoef.setCellValueFactory(c -> new SimpleFloatProperty(c.getValue().getCoef()).asObject());
        colCoef.setMaxWidth(1f * Integer.MAX_VALUE * 12);
        colCoef.setStyle("-fx-alignment: CENTER;");

        colActions.setMaxWidth(1f * Integer.MAX_VALUE * 15);
        colActions.setStyle("-fx-alignment: CENTER;");
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
            private final Button be = new Button("✎");
            private final Button bd = new Button("✕");
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
        ComboBox<Etudiant> cboEt = new ComboBox<>(FXCollections.observableArrayList(EtudiantDAO.getAll()));
        cboEt.setPromptText("-- Choisir --");
        ComboBox<Matiere> cboMat = new ComboBox<>(FXCollections.observableArrayList(MatiereDAO.getAll()));
        cboMat.setPromptText("-- Choisir --");
        TextField tfNote = new TextField();
        tfNote.setPromptText("ex: 15.5");

        var form = FxHelper.buildForm(
                FxHelper.labeledCombo("Etudiant", cboEt),
                FxHelper.labeledCombo("Matiere", cboMat),
                FxHelper.labeledField("Note (0-20)", tfNote)
        );

        ModalHelper.show("Nouvelle Note", form, () -> {
            if (cboEt.getValue() == null) {
                Toast.error("Selectionnez un etudiant.");
                return false;
            }
            if (cboMat.getValue() == null) {
                Toast.error("Selectionnez une matiere.");
                return false;
            }
            if (FxHelper.isValidNote(tfNote.getText())) {
                Toast.error("Note entre 0 et 20.");
                return false;
            }
            int ne = cboEt.getValue().getNumEtudiant(), nm = cboMat.getValue().getNumMat();
            if (NoteDAO.exists(ne, nm)) {
                Toast.error("Cette note existe deja.");
                return false;
            }
            if (NoteDAO.insert(new Note(ne, nm, Float.parseFloat(tfNote.getText().trim())))) {
                Toast.success("Note ajoutee (moyenne recalculee).");
                refreshData();
                return true;
            }
            Toast.error("Erreur ajout.");
            return false;
        });
    }

    private void openEdit(NoteView nv) {
        Label ctx = new Label(nv.getNomEtudiant() + " - " + nv.getDesignMatiere());
        ctx.getStyleClass().add("form-label");
        TextField tfNote = new TextField(String.format("%.1f", nv.getNote()));
        var form = FxHelper.buildForm(
                new javafx.scene.layout.VBox(4, ctx),
                FxHelper.labeledField("Nouvelle note (0-20)", tfNote)
        );
        ModalHelper.show("Modifier Note", form, () -> {
            if (FxHelper.isValidNote(tfNote.getText())) {
                Toast.error("Note entre 0 et 20.");
                return false;
            }
            if (NoteDAO.update(new Note(nv.getNumEtudiant(), nv.getNumMat(), Float.parseFloat(tfNote.getText().trim())))) {
                Toast.success("Note modifiee.");
                refreshData();
                return true;
            }
            Toast.error("Erreur modification.");
            return false;
        });
    }

    private void confirmDel(NoteView nv) {
        ModalHelper.confirm("Supprimer",
                "Supprimer la note de " + nv.getNomEtudiant() + " en " + nv.getDesignMatiere() + " ?",
                "Supprimer", "btn-modal-danger", () -> {
                    if (NoteDAO.delete(nv.getNumEtudiant(), nv.getNumMat())) {
                        Toast.success("Note supprimee.");
                        refreshData();
                    } else Toast.error("Erreur suppression.");
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
        DataService.loadAsync(NoteDAO::getAllView, d -> {
            allData = d;
            page = 0;
            applyFilter();
        }, tablePane);
    }

    private void applyFilter() {
        String kw = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        filtered = kw.isEmpty() ? new ArrayList<>(allData) : allData.stream()
                                                             .filter(n -> n.getNomEtudiant().toLowerCase().contains(kw)
                                                                          || n.getDesignMatiere().toLowerCase().contains(kw)
                                                                          || String.format("%.1f", n.getNote()).contains(kw)
                                                                          || String.valueOf(n.getNumEtudiant()).contains(kw))
                                                             .toList();
        updateTable();
    }

    private void updateTable() {
        int tp = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));
        page = Math.min(page, tp - 1);
        int from = page * PAGE_SIZE, to = Math.min(from + PAGE_SIZE, filtered.size());
        table.setItems(FXCollections.observableArrayList(filtered.subList(from, to)));
        lblCount.setText(filtered.size() + " note(s)");
        lblPage.setText("Page " + (page + 1) + " / " + tp);
        btnPrev.setDisable(page <= 0);
        btnNext.setDisable(page >= tp - 1);
    }
}

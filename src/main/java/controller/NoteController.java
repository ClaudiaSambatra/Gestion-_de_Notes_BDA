package controller;

import dao.EtudiantDAO;
import dao.MatiereDAO;
import dao.NoteDAO;
import javafx.beans.property.*;
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
    private List<NoteView> allData = new ArrayList<>(), filtered = new ArrayList<>();
    private int page = 0;
    private static final int PS = 15;
    private Debouncer deb;
    private String loggedUsername = "systeme";

    /**
     * Called by UserMainController to pass the logged user's login for audit
     */
    public void setLoggedUsername(String u) {
        this.loggedUsername = u;
    }

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colEtudiant.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNumEtudiant() + " - " + c.getValue().getNomEtudiant()));
        colEtudiant.setMaxWidth(1f * Integer.MAX_VALUE * 28);
        colMatiere.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDesignMatiere()));
        colMatiere.setMaxWidth(1f * Integer.MAX_VALUE * 26);
        colNote.setCellValueFactory(c -> new SimpleFloatProperty(c.getValue().getNote()).asObject());
        colNote.setMaxWidth(1f * Integer.MAX_VALUE * 15);
        colNote.setStyle("-fx-alignment:CENTER;");
        colNote.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Float it, boolean em) {
                super.updateItem(it, em);
                setAlignment(Pos.CENTER);
                if (em || it == null) {
                    setText(null);
                    setStyle("-fx-alignment:CENTER;");
                    return;
                }
                setText(String.format("%.1f", it));
                String c = it < 10 ? "#ef4444" : it >= 16 ? "#22c55e" : "";
                setStyle("-fx-alignment:CENTER;" + (c.isEmpty() ? "" : "-fx-text-fill:" + c + ";-fx-font-weight:bold;"));
            }
        });
        colCoef.setCellValueFactory(c -> new SimpleFloatProperty(c.getValue().getCoef()).asObject());
        colCoef.setMaxWidth(1f * Integer.MAX_VALUE * 12);
        colCoef.setStyle("-fx-alignment:CENTER;");
        colActions.setMaxWidth(1f * Integer.MAX_VALUE * 15);
        colActions.setStyle("-fx-alignment:CENTER;");
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button be = new Button("✎"), bd = new Button("✕");
            private final HBox bx = new HBox(6, be, bd);

            {
                be.getStyleClass().addAll("btn-icon", "btn-icon-edit");
                bd.getStyleClass().addAll("btn-icon", "btn-icon-delete");
                bx.setAlignment(Pos.CENTER);
                be.setOnAction(e -> editM(getTableView().getItems().get(getIndex())));
                bd.setOnAction(e -> delM(getTableView().getItems().get(getIndex())));
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
        ComboBox<Etudiant> ce = new ComboBox<>(FXCollections.observableArrayList(EtudiantDAO.getAll()));
        ce.setPromptText("-- Etudiant --");
        ComboBox<Matiere> cm = new ComboBox<>(FXCollections.observableArrayList(MatiereDAO.getAll()));
        cm.setPromptText("-- Matiere --");
        TextField tn = new TextField();
        tn.setPromptText("ex:15.5");
        var f = FxHelper.buildForm(FxHelper.labeledCombo("Etudiant", ce), FxHelper.labeledCombo("Matiere", cm), FxHelper.labeledField("Note (0-20)", tn));
        ModalHelper.show("Nouvelle Note", f, () -> {
            if (ce.getValue() == null) {
                Toast.error("Selectionnez un etudiant.");
                return false;
            }
            if (cm.getValue() == null) {
                Toast.error("Selectionnez une matiere.");
                return false;
            }
            if (FxHelper.isValidNote(tn.getText())) {
                Toast.error("Note entre 0 et 20.");
                return false;
            }
            int ne = ce.getValue().getNumEtudiant(), nm = cm.getValue().getNumMat();
            if (NoteDAO.exists(ne, nm)) {
                Toast.error("Note existante. Modifier.");
                return false;
            }
            if (NoteDAO.insert(new Note(ne, nm, Float.parseFloat(tn.getText().trim())), loggedUsername)) {
                Toast.success("Note ajoutee.");
                refreshData();
                return true;
            }
            Toast.error("Erreur.");
            return false;
        });
    }

    private void editM(NoteView nv) {
        Label ctx = new Label(nv.getNomEtudiant() + " - " + nv.getDesignMatiere());
        ctx.getStyleClass().add("form-label");
        TextField tn = new TextField(String.format("%.1f", nv.getNote()));
        var f = FxHelper.buildForm(new javafx.scene.layout.VBox(4, ctx), FxHelper.labeledField("Nouvelle note", tn));
        ModalHelper.show("Modifier Note", f, () -> {
            if (FxHelper.isValidNote(tn.getText())) {
                Toast.error("Note entre 0 et 20.");
                return false;
            }
            if (NoteDAO.update(new Note(nv.getNumEtudiant(), nv.getNumMat(), Float.parseFloat(tn.getText().trim())), loggedUsername)) {
                Toast.success("Modifiee.");
                refreshData();
                return true;
            }
            Toast.error("Erreur.");
            return false;
        });
    }

    private void delM(NoteView nv) {
        ModalHelper.confirm("Supprimer", "Supprimer note de " + nv.getNomEtudiant() + " en " + nv.getDesignMatiere() + " ?", "Supprimer", "btn-modal-danger", () -> {
            if (NoteDAO.delete(nv.getNumEtudiant(), nv.getNumMat(), loggedUsername)) {
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
        DataService.loadAsync(NoteDAO::getAllView, d -> {
            allData = d;
            page = 0;
            filter();
        }, tablePane);
    }

    private void filter() {
        String k = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        filtered = k.isEmpty() ? new ArrayList<>(allData) : allData.stream().filter(n -> n.getNomEtudiant().toLowerCase().contains(k) || n.getDesignMatiere().toLowerCase().contains(k) || String.format("%.1f", n.getNote()).contains(k)).toList();
        upd();
    }

    private void upd() {
        int tp = Math.max(1, (int) Math.ceil((double) filtered.size() / PS));
        page = Math.min(page, tp - 1);
        int f = page * PS, t = Math.min(f + PS, filtered.size());
        table.setItems(FXCollections.observableArrayList(filtered.subList(f, t)));
        lblCount.setText(filtered.size() + " note(s)");
        lblPage.setText("Page " + (page + 1) + " / " + tp);
        btnPrev.setDisable(page <= 0);
        btnNext.setDisable(page >= tp - 1);
    }
}

package controller;

import dao.AuditDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import model.AuditNote;
import util.*;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AuditController implements Refreshable {
    @FXML
    private TextField txtSearch;
    @FXML
    private StackPane tablePane;
    @FXML
    private TableView<AuditNote> table;
    @FXML
    private TableColumn<AuditNote, String> colType, colDate, colEtudiant, colNom, colMatiere, colUtilisateur;
    @FXML
    private TableColumn<AuditNote, Float> colAncienne, colNouvelle;
    @FXML
    private Label lblInserts, lblUpdates, lblDeletes, lblTotal, lblPage;
    @FXML
    private Button btnPrev, btnNext;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private List<AuditNote> allData = new ArrayList<>(), filtered = new ArrayList<>();
    private int page = 0;
    private static final int PS = 18;
    private String typeFilter = null;
    private Debouncer deb;

    // ── French labels ─────────────────────────────────────────────────────────

    /**
     * Returns the French display name for a DB operation string.
     */
    private static String toFrench(String op) {
        return switch (op) {
            case "INSERT" -> "INSERTION";
            case "UPDATE" -> "MODIFICATION";
            case "DELETE" -> "SUPPRESSION";
            default -> op;
        };
    }

    // ── Initialization ────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // Operation column — badge shows French label, CSS class uses DB value
        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTypeOperation()));
        colType.setMaxWidth(1f * Integer.MAX_VALUE * 14);
        colType.setStyle("-fx-alignment:CENTER;");
        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String it, boolean em) {
                super.updateItem(it, em);
                if (em || it == null) {
                    setGraphic(null);
                    return;
                }
                Label b = new Label(toFrench(it));                     // French text
                b.getStyleClass().addAll("badge", "badge-" + it.toLowerCase()); // DB-based CSS class
                setGraphic(b);
                setAlignment(Pos.CENTER);
            }
        });

        colDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDateOperation() != null ? c.getValue().getDateOperation().format(DTF) : ""));
        colDate.setMaxWidth(1f * Integer.MAX_VALUE * 16);

        colEtudiant.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getNumEtudiant())));
        colEtudiant.setMaxWidth(1f * Integer.MAX_VALUE * 8);
        colEtudiant.setStyle("-fx-alignment:CENTER;");

        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colNom.setMaxWidth(1f * Integer.MAX_VALUE * 14);

        colMatiere.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDesign()));
        colMatiere.setMaxWidth(1f * Integer.MAX_VALUE * 14);

        colAncienne.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getNoteAncien()));
        colAncienne.setMaxWidth(1f * Integer.MAX_VALUE * 8);
        colAncienne.setStyle("-fx-alignment:CENTER;");
        colAncienne.setCellFactory(col -> fc());

        colNouvelle.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getNoteNouv()));
        colNouvelle.setMaxWidth(1f * Integer.MAX_VALUE * 8);
        colNouvelle.setStyle("-fx-alignment:CENTER;");
        colNouvelle.setCellFactory(col -> fc());

        colUtilisateur.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUtilisateur()));
        colUtilisateur.setMaxWidth(1f * Integer.MAX_VALUE * 14);

        // Row colour uses DB values (row-insert / row-update / row-delete CSS classes)
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(AuditNote it, boolean em) {
                super.updateItem(it, em);
                getStyleClass().removeAll("row-insert", "row-update", "row-delete");
                if (it != null) {
                    switch (it.getTypeOperation()) {
                        case "INSERT" -> getStyleClass().add("row-insert");
                        case "UPDATE" -> getStyleClass().add("row-update");
                        case "DELETE" -> getStyleClass().add("row-delete");
                    }
                }
            }
        });

        deb = new Debouncer(Duration.millis(300), () -> {
            page = 0;
            applyFilter();
        });
        txtSearch.textProperty().addListener((o, a, v) -> deb.trigger());

        // Auto-refresh every 10 s
        Timeline poll = new Timeline(new KeyFrame(Duration.seconds(10), e ->
                DataService.loadAsync(AuditDAO::getAll, d -> {
                    allData = d;
                    applyFilter();
                }, null)));
        poll.setCycleCount(Timeline.INDEFINITE);
        poll.play();

        refreshData();
    }

    // ── Filter actions (filter still uses DB values internally) ───────────────

    @FXML
    private void handleRefresh() {
        refreshData();
        Toast.info("Audit actualise.");
    }

    @FXML
    private void handleFilterAll() {
        typeFilter = null;
        page = 0;
        applyFilter();
    }

    @FXML
    private void handleFilterInsert() {
        typeFilter = "INSERT";
        page = 0;
        applyFilter();
    }

    @FXML
    private void handleFilterUpdate() {
        typeFilter = "UPDATE";
        page = 0;
        applyFilter();
    }

    @FXML
    private void handleFilterDelete() {
        typeFilter = "DELETE";
        page = 0;
        applyFilter();
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
        DataService.loadAsync(AuditDAO::getAll, d -> {
            allData = d;
            page = 0;
            typeFilter = null;
            applyFilter();
        }, tablePane);
    }

    // ── Filtering & pagination ────────────────────────────────────────────────

    private void applyFilter() {
        String k = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        filtered = allData.stream()
                .filter(a -> typeFilter == null || a.getTypeOperation().equals(typeFilter))
                .filter(a -> {
                    if (k.isEmpty()) return true;
                    // Also match French names so the user can type "insertion" etc.
                    return (a.getNom() != null && a.getNom().toLowerCase().contains(k))
                            || (a.getDesign() != null && a.getDesign().toLowerCase().contains(k))
                            || a.getTypeOperation().toLowerCase().contains(k)
                            || toFrench(a.getTypeOperation()).toLowerCase().contains(k)
                            || String.valueOf(a.getNumEtudiant()).contains(k)
                            || (a.getUtilisateur() != null && a.getUtilisateur().toLowerCase().contains(k));
                }).toList();

        long ins = filtered.stream().filter(a -> "INSERT".equals(a.getTypeOperation())).count();
        long up = filtered.stream().filter(a -> "UPDATE".equals(a.getTypeOperation())).count();
        long del = filtered.stream().filter(a -> "DELETE".equals(a.getTypeOperation())).count();
        lblInserts.setText(String.valueOf(ins));
        lblUpdates.setText(String.valueOf(up));
        lblDeletes.setText(String.valueOf(del));
        lblTotal.setText(String.valueOf(filtered.size()));
        upd();
    }

    private void upd() {
        int tp = Math.max(1, (int) Math.ceil((double) filtered.size() / PS));
        page = Math.min(page, tp - 1);
        int f = page * PS, t = Math.min(f + PS, filtered.size());
        table.setItems(FXCollections.observableArrayList(filtered.subList(f, t)));
        lblPage.setText("Page " + (page + 1) + " / " + tp);
        btnPrev.setDisable(page <= 0);
        btnNext.setDisable(page >= tp - 1);
    }

    private static TableCell<AuditNote, Float> fc() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Float it, boolean em) {
                super.updateItem(it, em);
                setAlignment(Pos.CENTER);
                if (em || it == null) {
                    setText("-");
                    setStyle("-fx-alignment:CENTER;-fx-text-fill:-gn-text-hint;");
                } else {
                    setText(String.format("%.1f", it));
                    setStyle("-fx-alignment:CENTER;");
                }
            }
        };
    }
}
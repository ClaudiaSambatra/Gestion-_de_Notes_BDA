package controller;

import dao.AuditDAO;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
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
    private List<AuditNote> allData = new ArrayList<>();
    private List<AuditNote> filtered = new ArrayList<>();
    private int page = 0;
    private static final int PAGE_SIZE = 18;
    private String typeFilter = null;
    private Debouncer debouncer;

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        colType.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTypeOperation()));
        colType.setMaxWidth(1f * Integer.MAX_VALUE * 12);
        colType.setStyle("-fx-alignment: CENTER;");
        colType.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }
                Label badge = new Label(item);
                badge.getStyleClass().addAll("badge", "badge-" + item.toLowerCase());
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        colDate.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDateOperation() != null ? c.getValue().getDateOperation().format(DTF) : ""));
        colDate.setMaxWidth(1f * Integer.MAX_VALUE * 16);

        colEtudiant.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getNumEtudiant())));
        colEtudiant.setMaxWidth(1f * Integer.MAX_VALUE * 8);
        colEtudiant.setStyle("-fx-alignment: CENTER;");

        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colNom.setMaxWidth(1f * Integer.MAX_VALUE * 16);

        colMatiere.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDesign()));
        colMatiere.setMaxWidth(1f * Integer.MAX_VALUE * 14);

        colAncienne.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getNoteAncien()));
        colAncienne.setMaxWidth(1f * Integer.MAX_VALUE * 8);
        colAncienne.setStyle("-fx-alignment: CENTER;");
        colAncienne.setCellFactory(col -> floatCell());

        colNouvelle.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getNoteNouv()));
        colNouvelle.setMaxWidth(1f * Integer.MAX_VALUE * 8);
        colNouvelle.setStyle("-fx-alignment: CENTER;");
        colNouvelle.setCellFactory(col -> floatCell());

        colUtilisateur.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUtilisateur()));
        colUtilisateur.setMaxWidth(1f * Integer.MAX_VALUE * 12);

        table.setRowFactory(tv -> new TableRow<>() {
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

        debouncer = new Debouncer(Duration.millis(300), () -> {
            page = 0;
            applyFilter();
        });
        txtSearch.textProperty().addListener((o, a, v) -> debouncer.trigger());

        Timeline autoRefresh = new Timeline(new KeyFrame(Duration.seconds(10), e -> silentRefresh()));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();

        refreshData();
    }


    private void silentRefresh() {
        DataService.loadAsync(AuditDAO::getAll, d -> {
            allData = d;
            applyFilter();
        }, null);
    }

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
        DataService.loadAsync(AuditDAO::getAll, d -> {
            allData = d;
            page = 0;
            typeFilter = null;
            applyFilter();
        }, tablePane);
    }

    private void applyFilter() {
        String kw = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        filtered = allData.stream()
                .filter(a -> typeFilter == null || a.getTypeOperation().equals(typeFilter))
                .filter(a -> {
                    if (kw.isEmpty()) return true;
                    return (a.getNom() != null && a.getNom().toLowerCase().contains(kw))
                            || (a.getDesign() != null && a.getDesign().toLowerCase().contains(kw))
                            || a.getTypeOperation().toLowerCase().contains(kw)
                            || String.valueOf(a.getNumEtudiant()).contains(kw)
                            || (a.getUtilisateur() != null && a.getUtilisateur().toLowerCase().contains(kw));
                }).toList();
        updateCounters();
        updateTable();
    }

    private void updateCounters() {
        long ins = filtered.stream().filter(a -> "INSERT".equals(a.getTypeOperation())).count();
        long upd = filtered.stream().filter(a -> "UPDATE".equals(a.getTypeOperation())).count();
        long del = filtered.stream().filter(a -> "DELETE".equals(a.getTypeOperation())).count();
        lblInserts.setText(String.valueOf(ins));
        lblUpdates.setText(String.valueOf(upd));
        lblDeletes.setText(String.valueOf(del));
        lblTotal.setText(String.valueOf(filtered.size()));
    }

    private void updateTable() {
        int tp = Math.max(1, (int) Math.ceil((double) filtered.size() / PAGE_SIZE));
        page = Math.min(page, tp - 1);
        int from = page * PAGE_SIZE, to = Math.min(from + PAGE_SIZE, filtered.size());
        table.setItems(FXCollections.observableArrayList(filtered.subList(from, to)));
        lblPage.setText("Page " + (page + 1) + " / " + tp);
        btnPrev.setDisable(page <= 0);
        btnNext.setDisable(page >= tp - 1);
    }

    private static TableCell<AuditNote, Float> floatCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Float item, boolean empty) {
                super.updateItem(item, empty);
                setAlignment(Pos.CENTER);
                if (empty || item == null) {
                    setText("-");
                    setStyle("-fx-alignment:CENTER; -fx-text-fill: -gn-text-hint;");
                } else {
                    setText(String.format("%.1f", item));
                    setStyle("-fx-alignment:CENTER;");
                }
            }
        };
    }
}

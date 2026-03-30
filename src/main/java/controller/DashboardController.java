package controller;

import dao.AuditDAO;
import dao.StatsDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.AuditNote;
import model.Etudiant;
import util.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardController implements Refreshable {

    @FXML
    private Label statEtudiants, statMatieres, statNotes, statMoyenne, statTaux;
    @FXML
    private BarChart<String, Number> chartTop;
    @FXML
    private CategoryAxis topXAxis;
    @FXML
    private NumberAxis topYAxis;
    @FXML
    private PieChart chartDistrib;
    @FXML
    private VBox recentActivity;
    @FXML
    private StackPane chartTopPane, chartDistribPane;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    @FXML
    public void initialize() {
        refreshData();
    }

    @FXML
    private void handleRefresh() {
        refreshData();
        Toast.info("Dashboard actualise.");
    }

    @Override
    public void refreshData() {
        DataService.loadAsync(() -> {
            int ne = StatsDAO.countEtudiants(), nm = StatsDAO.countMatieres(), nn = StatsDAO.countNotes();
            float moy = StatsDAO.moyenneGenerale(), taux = StatsDAO.tauxReussite();
            List<Etudiant> top = StatsDAO.topEtudiants(7);
            Map<String, Integer> dist = StatsDAO.noteDistribution();
            List<AuditNote> audits = AuditDAO.getAll();
            return new Object[]{ne, nm, nn, moy, taux, top, dist, audits};
        }, data -> {
            statEtudiants.setText(String.valueOf(data[0]));
            statMatieres.setText(String.valueOf(data[1]));
            statNotes.setText(String.valueOf(data[2]));
            float moy = (float) data[3];
            statMoyenne.setText(moy > 0 ? String.format("%.2f", moy) : "--");
            float taux = (float) data[4];
            statTaux.setText(taux > 0 ? String.format("%.0f%%", taux) : "--");

            @SuppressWarnings("unchecked") List<Etudiant> top = (List<Etudiant>) data[5];
            loadTopChart(top);

            @SuppressWarnings("unchecked") Map<String, Integer> dist = (Map<String, Integer>) data[6];
            loadDistribChart(dist);

            @SuppressWarnings("unchecked") List<AuditNote> audits = (List<AuditNote>) data[7];
            loadRecentActivity(audits);
        }, chartTopPane);
    }

    private void loadTopChart(List<Etudiant> top) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Moyenne");
        for (Etudiant e : top) {
            String lbl = e.getNom().length() > 14 ? e.getNom().substring(0, 14) + "." : e.getNom();
            series.getData().add(new XYChart.Data<>(lbl, e.getMoyenne()));
        }
        chartTop.getData().clear();
        chartTop.getData().add(series);
        chartTop.setLegendVisible(false);
        topYAxis.setAutoRanging(false);
        topYAxis.setLowerBound(0);
        topYAxis.setUpperBound(20);
        topYAxis.setTickUnit(5);
    }

    private void loadDistribChart(Map<String, Integer> dist) {
        chartDistrib.setData(FXCollections.observableArrayList(
                dist.entrySet().stream()
                        .map(e -> new PieChart.Data(e.getKey() + " (" + e.getValue() + ")", e.getValue()))
                        .toList()));
        chartDistrib.setLegendVisible(true);
        chartDistrib.setLabelsVisible(false);
    }

    private void loadRecentActivity(List<AuditNote> audits) {
        recentActivity.getChildren().clear();
        int limit = Math.min(8, audits.size());
        if (limit == 0) {
            Label empty = new Label("Aucune activite recente");
            empty.getStyleClass().add("activity-empty");
            recentActivity.getChildren().add(empty);
            return;
        }
        for (int i = 0; i < limit; i++) {
            AuditNote a = audits.get(i);
            String icon = switch (a.getTypeOperation()) {
                case "INSERT" -> "\u25B2";
                case "UPDATE" -> "\u25C6";
                case "DELETE" -> "\u25BC";
                default -> "\u25CF";
            };
            Label line = new Label(icon + "  " + a.getTypeOperation() + " - "
                    + (a.getNom() != null ? a.getNom() : "?") + " / "
                    + (a.getDesign() != null ? a.getDesign() : "?"));
            line.getStyleClass().addAll("activity-line", "activity-" + a.getTypeOperation().toLowerCase());
            Label date = new Label(a.getDateOperation() != null ? a.getDateOperation().format(DTF) : "");
            date.getStyleClass().add("activity-date");
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            HBox row = new HBox(10, line, sp, date);
            row.getStyleClass().add("activity-row");
            row.setAlignment(Pos.CENTER_LEFT);
            recentActivity.getChildren().add(row);
        }
    }
}

package controller;

import dao.AuditDAO;
import dao.StatsDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import model.AuditNote;
import model.Etudiant;
import util.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class DashboardController implements Refreshable {

    // ── Stat labels ───────────────────────────────────────────────────────────
    @FXML
    private Label statEtudiants, statMatieres, statNotes, statMoyenne, statTaux;

    // ── Row 1: Bar + Pie ──────────────────────────────────────────────────────
    @FXML
    private BarChart<String, Number> chartTop;
    @FXML
    private CategoryAxis topXAxis;
    @FXML
    private NumberAxis topYAxis;
    @FXML
    private PieChart chartDistrib;
    @FXML
    private StackPane chartTopPane;

    // ── Row 2: Line + Area (NEW) ──────────────────────────────────────────────
    @FXML
    private LineChart<String, Number> chartLine;
    @FXML
    private CategoryAxis lineXAxis;
    @FXML
    private NumberAxis lineYAxis;
    @FXML
    private AreaChart<String, Number> chartArea;
    @FXML
    private CategoryAxis areaXAxis;
    @FXML
    private NumberAxis areaYAxis;

    // ── Row 3: KPI section (NEW) ──────────────────────────────────────────────
    @FXML
    private VBox kpiSection;

    // ── Row 4: Activity (admin only) ──────────────────────────────────────────
    @FXML
    private VBox recentActivity;
    @FXML
    private VBox activitySection;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    /**
     * Whether the "Activite Recente" card should be shown (Admin = true, User = false).
     */
    private boolean showActivity = true;

    /**
     * Called by UserMainController right after loading this view.
     */
    public void setShowActivity(boolean show) {
        this.showActivity = show;
        if (activitySection != null) {
            activitySection.setVisible(show);
            activitySection.setManaged(show);
        }
    }

    @FXML
    public void initialize() {
        if (activitySection != null) {
            activitySection.setVisible(showActivity);
            activitySection.setManaged(showActivity);
        }
        refreshData();
    }

    @FXML
    private void handleRefresh() {
        refreshData();
        Toast.info("Dashboard actualise.");
    }

    private static String toFrench(String op) {
        return switch (op) {
            case "INSERT" -> "INSERTION";
            case "UPDATE" -> "MODIFICATION";
            case "DELETE" -> "SUPPRESSION";
            default -> op;
        };
    }

    @Override
    public void refreshData() {
        DataService.loadAsync(() -> {
            int ne = StatsDAO.countEtudiants();
            int nm = StatsDAO.countMatieres();
            int nn = StatsDAO.countNotes();
            float moy = StatsDAO.moyenneGenerale();
            float taux = StatsDAO.tauxReussite();
            List<Etudiant> top = StatsDAO.topEtudiants(7);
            Map<String, Integer> dist = StatsDAO.noteDistribution();
            List<AuditNote> audits = AuditDAO.getAll();
            return new Object[]{ne, nm, nn, moy, taux, top, dist, audits};
        }, data -> {

            int ne = (int) data[0];
            int nm = (int) data[1];
            int nn = (int) data[2];
            float moy = (float) data[3];
            float taux = (float) data[4];

            // ── Stat labels ───────────────────────────────────────────────────
            statEtudiants.setText(String.valueOf(ne));
            statMatieres.setText(String.valueOf(nm));
            statNotes.setText(String.valueOf(nn));
            statMoyenne.setText(moy > 0 ? String.format("%.2f", moy) : "--");
            statTaux.setText(taux > 0 ? String.format("%.0f%%", taux) : "--");

            @SuppressWarnings("unchecked") List<Etudiant> top = (List<Etudiant>) data[5];
            @SuppressWarnings("unchecked") Map<String, Integer> dist = (Map<String, Integer>) data[6];

            // ── BarChart: Top students ────────────────────────────────────────
            XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
            barSeries.setName("Moyenne");
            for (Etudiant e : top) {
                String label = e.getNom().length() > 14 ? e.getNom().substring(0, 14) + "." : e.getNom();
                barSeries.getData().add(new XYChart.Data<>(label, e.getMoyenne()));
            }
            chartTop.getData().clear();
            chartTop.getData().add(barSeries);
            chartTop.setLegendVisible(false);
            topYAxis.setAutoRanging(false);
            topYAxis.setLowerBound(0);
            topYAxis.setUpperBound(20);
            topYAxis.setTickUnit(5);

            // ── PieChart: Distribution ────────────────────────────────────────
            chartDistrib.setData(FXCollections.observableArrayList(
                    dist.entrySet().stream()
                            .filter(e -> e.getValue() > 0)
                            .map(e -> new PieChart.Data(e.getKey() + " (" + e.getValue() + ")", e.getValue()))
                            .toList()));
            chartDistrib.setLegendVisible(true);
            chartDistrib.setLabelsVisible(false);

            // ── LineChart: Top students curve ─────────────────────────────────
            XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
            lineSeries.setName("Moyenne");
            for (Etudiant e : top) {
                String label = e.getNom().length() > 11 ? e.getNom().substring(0, 11) + "." : e.getNom();
                lineSeries.getData().add(new XYChart.Data<>(label, e.getMoyenne()));
            }
            chartLine.getData().clear();
            chartLine.getData().add(lineSeries);
            chartLine.setLegendVisible(false);
            lineYAxis.setAutoRanging(false);
            lineYAxis.setLowerBound(0);
            lineYAxis.setUpperBound(20);
            lineYAxis.setTickUnit(5);

            // ── AreaChart: Distribution as a curve ───────────────────────────
            XYChart.Series<String, Number> areaSeries = new XYChart.Series<>();
            areaSeries.setName("Etudiants");
            for (Map.Entry<String, Integer> entry : dist.entrySet()) {
                areaSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            chartArea.getData().clear();
            chartArea.getData().add(areaSeries);
            chartArea.setLegendVisible(false);

            // ── KPI Progress bars ─────────────────────────────────────────────
            kpiSection.getChildren().clear();

            // Taux de réussite
            kpiSection.getChildren().add(buildKpi(
                    "Taux de reussite", taux / 100.0,
                    taux > 0 ? String.format("%.0f%%", taux) : "--",
                    "#16a34a"));

            // Couverture des notes  (notes saisies vs notes possibles)
            int possible = ne > 0 && nm > 0 ? ne * nm : 0;
            double coverage = possible > 0 ? Math.min((double) nn / possible, 1.0) : 0;
            kpiSection.getChildren().add(buildKpi(
                    "Couverture des notes (" + nn + " / " + possible + ")",
                    coverage,
                    String.format("%.0f%%", coverage * 100),
                    "#0ea5e9"));

            // Moyenne generale / 20
            kpiSection.getChildren().add(buildKpi(
                    "Moyenne generale / 20",
                    moy > 0 ? moy / 20.0 : 0,
                    moy > 0 ? String.format("%.2f / 20", moy) : "--",
                    "#f59e0b"));

            // Proportion etudiants avec des notes
            long withNotes = top.stream().filter(e -> e.getMoyenne() > 0).count();
            double notedRatio = ne > 0 ? (double) withNotes / ne : 0;
            kpiSection.getChildren().add(buildKpi(
                    "Etudiants avec au moins une note",
                    notedRatio,
                    ne > 0 ? withNotes + " / " + ne : "--",
                    "#a855f7"));

            // ── Activity feed (admin only) ────────────────────────────────────
            if (activitySection != null) {
                activitySection.setVisible(showActivity);
                activitySection.setManaged(showActivity);
            }
            if (!showActivity) return;

            @SuppressWarnings("unchecked") List<AuditNote> audits = (List<AuditNote>) data[7];
            recentActivity.getChildren().clear();
            int lim = Math.min(8, audits.size());
            if (lim == 0) {
                Label em = new Label("Aucune activite recente");
                em.getStyleClass().add("activity-empty");
                recentActivity.getChildren().add(em);
                return;
            }
            for (int i = 0; i < lim; i++) {
                AuditNote a = audits.get(i);
                String ic = switch (a.getTypeOperation()) {
                    case "INSERT" -> "▲";
                    case "UPDATE" -> "◆";
                    case "DELETE" -> "▼";
                    default -> "●";
                };
                String typeFr = toFrench(a.getTypeOperation());
                String typeCss = a.getTypeOperation().toLowerCase();
                Label ln = new Label(ic + "  " + typeFr + " - "
                        + (a.getNom() != null ? a.getNom() : "?")
                        + " / " + (a.getDesign() != null ? a.getDesign() : "?"));
                ln.getStyleClass().addAll("activity-line", "activity-" + typeCss);
                Label dt = new Label(a.getDateOperation() != null
                        ? a.getDateOperation().format(DTF) : "");
                dt.getStyleClass().add("activity-date");
                Region sp = new Region();
                HBox.setHgrow(sp, Priority.ALWAYS);
                HBox row = new HBox(10, ln, sp, dt);
                row.getStyleClass().add("activity-row");
                row.setAlignment(Pos.CENTER_LEFT);
                recentActivity.getChildren().add(row);
            }

        }, chartTopPane);
    }

    /**
     * Build a labeled KPI row: [label] [progress bar ─────────] [value]
     */
    private HBox buildKpi(String label, double ratio, String valueText, String color) {
        Label lbl = new Label(label);
        lbl.getStyleClass().add("form-label");
        lbl.setMinWidth(250);

        ProgressBar pb = new ProgressBar(Math.max(0, Math.min(ratio, 1.0)));
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setPrefHeight(12);
        HBox.setHgrow(pb, Priority.ALWAYS);
        pb.getStyleClass().add("kpi-bar");
        // Override the accent color per bar using inline style
        pb.setStyle("-fx-accent: " + color + ";");

        Label val = new Label(valueText);
        val.getStyleClass().add("kpi-value");
        val.setMinWidth(80);
        val.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(14, lbl, pb, val);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("kpi-row");
        return row;
    }
}
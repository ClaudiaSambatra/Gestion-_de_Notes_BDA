package controller;

import dao.AuditDAO;
import dao.StatsDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
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
    private VBox activitySection;      // the whole "Activite Recente" card
    @FXML
    private StackPane chartTopPane;
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    /**
     * Whether the "Activite Recente" card should be shown (Admin = true, User = false).
     */
    private boolean showActivity = true;

    /**
     * Called by UserMainController right after loading this view to hide the
     * recent-activity section that is only relevant for admins.
     */
    public void setShowActivity(boolean show) {
        this.showActivity = show;
        // activitySection is injected after initialize(), so guard against null
        if (activitySection != null) {
            activitySection.setVisible(show);
            activitySection.setManaged(show);
        }
    }

    @FXML
    public void initialize() {
        // Apply visibility preference injected before initialize (rare) or after (usual)
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

    /**
     * Translate DB operation name to French for display in the activity feed.
     */
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
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            s.setName("Moyenne");
            for (Etudiant e : top) {
                String l = e.getNom().length() > 14 ? e.getNom().substring(0, 14) + "." : e.getNom();
                s.getData().add(new XYChart.Data<>(l, e.getMoyenne()));
            }
            chartTop.getData().clear();
            chartTop.getData().add(s);
            chartTop.setLegendVisible(false);
            topYAxis.setAutoRanging(false);
            topYAxis.setLowerBound(0);
            topYAxis.setUpperBound(20);
            topYAxis.setTickUnit(5);

            @SuppressWarnings("unchecked") Map<String, Integer> dist = (Map<String, Integer>) data[6];
            chartDistrib.setData(FXCollections.observableArrayList(
                    dist.entrySet().stream()
                            .map(e -> new PieChart.Data(e.getKey() + " (" + e.getValue() + ")", e.getValue()))
                            .toList()));
            chartDistrib.setLegendVisible(true);
            chartDistrib.setLabelsVisible(false);

            // ── Activity feed (only built if visible) ────────────────────────
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
                // Display French operation name; keep DB-value CSS class for colour
                String typeFr = toFrench(a.getTypeOperation());
                String typeCss = a.getTypeOperation().toLowerCase(); // insert / update / delete
                Label ln = new Label(ic + "  " + typeFr + " - "
                        + (a.getNom() != null ? a.getNom() : "?")
                        + " / " + (a.getDesign() != null ? a.getDesign() : "?"));
                ln.getStyleClass().addAll("activity-line", "activity-" + typeCss);
                Label dt = new Label(a.getDateOperation() != null ? a.getDateOperation().format(DTF) : "");
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
}
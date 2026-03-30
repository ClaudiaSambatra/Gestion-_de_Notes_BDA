package controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Utilisateur;
import util.*;

import java.util.HashMap;
import java.util.Map;

public class UserMainController {

    @FXML
    private StackPane rootStack;
    @FXML
    private VBox toastContainer;
    @FXML
    private StackPane contentArea;
    @FXML
    private Label lblUser, lblUserRole, lblUserInitials;
    @FXML
    private Button btnDashboard, btnEtudiants, btnMatieres, btnNotes, btnTheme, btnLogout;

    private record ViewEntry(Node node, Object controller) {
    }

    private final Map<String, ViewEntry> cache = new HashMap<>();
    private Button activeButton;
    private Utilisateur loggedUser;

    public void setLoggedUser(Utilisateur u) {
        this.loggedUser = u;
        lblUser.setText(u.getNomComplet());
        lblUserRole.setText(u.getRole());
        lblUserInitials.setText(u.getInitiales());
    }

    @FXML
    public void initialize() {
        AppContext.register(rootStack, toastContainer);

        btnDashboard.setOnAction(e -> navigateTo("dashboard", "/view/dashboard.fxml", btnDashboard));
        btnEtudiants.setOnAction(e -> navigateTo("etudiant", "/view/etudiant.fxml", btnEtudiants));
        btnMatieres.setOnAction(e -> navigateTo("matiere", "/view/matiere.fxml", btnMatieres));
        btnNotes.setOnAction(e -> navigateTo("note", "/view/note.fxml", btnNotes));

        btnTheme.setOnAction(e -> {
            AppContext.toggleTheme(rootStack);
            btnTheme.setText(AppContext.isDark(rootStack) ? "☀ Clair" : "☾ Sombre");
        });

        btnLogout.setOnAction(e -> {
            AppContext.unregister(rootStack);
            Stage stage = (Stage) rootStack.getScene().getWindow();
            stage.close();
            LoginController.openLogin("user", "GestNotes — Connexion Utilisateur");
        });

        navigateTo("dashboard", "/view/dashboard.fxml", btnDashboard);
    }

    private void navigateTo(String key, String fxml, Button btn) {
        if (activeButton != null) activeButton.getStyleClass().remove("nav-active");
        btn.getStyleClass().add("nav-active");
        activeButton = btn;

        ViewEntry entry = cache.computeIfAbsent(key, k -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                Node node = loader.load();
                Object ctrl = loader.getController();

                // Pass logged username to NoteController for audit trail
                if (ctrl instanceof NoteController nc && loggedUser != null) {
                    nc.setLoggedUsername(loggedUser.getLogin());
                }
                // Hide the "Activite Recente" card — it is for admins only
                if (ctrl instanceof DashboardController dc) {
                    dc.setShowActivity(false);
                }
                return new ViewEntry(node, ctrl);
            } catch (Exception ex) {
                AppLog.error("Load " + fxml, ex);
                return new ViewEntry(new Label("Erreur"), null);
            }
        });

        if (entry.controller() instanceof Refreshable r) r.refreshData();

        Node view = entry.node();
        if (!contentArea.getChildren().isEmpty()) {
            Node cur = contentArea.getChildren().get(0);
            FadeTransition out = new FadeTransition(Duration.millis(80), cur);
            out.setToValue(0);
            out.setOnFinished(e -> {
                contentArea.getChildren().setAll(view);
                fadeIn(view);
            });
            out.play();
        } else {
            contentArea.getChildren().setAll(view);
            fadeIn(view);
        }
    }

    private void fadeIn(Node n) {
        n.setOpacity(0);
        FadeTransition f = new FadeTransition(Duration.millis(150), n);
        f.setToValue(1);
        f.play();
    }
}
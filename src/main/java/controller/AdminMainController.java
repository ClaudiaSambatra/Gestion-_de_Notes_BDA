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

public class AdminMainController {

    @FXML
    private StackPane rootStack;
    @FXML
    private VBox toastContainer;
    @FXML
    private StackPane contentArea;
    @FXML
    private Label lblUser, lblUserRole, lblUserInitials;
    @FXML
    private Button btnDashboard, btnUsers, btnAudit, btnTheme, btnLogout;
    /**
     * Small ◁/▷ toggle placed next to the Utilisateurs nav button.
     */
    @FXML
    private Button btnToggleUsers;

    private record ViewEntry(Node node, Object controller) {
    }

    private final Map<String, ViewEntry> viewCache = new HashMap<>();
    private Button activeButton;
    /**
     * True when the Utilisateurs section is currently visible.
     */
    private boolean usersVisible = true;

    public void setLoggedUser(Utilisateur u) {
        lblUser.setText(u.getNomComplet());
        lblUserRole.setText(u.getRole());
        lblUserInitials.setText(u.getInitiales());
    }

    @FXML
    public void initialize() {
        AppContext.register(rootStack, toastContainer);

        btnDashboard.setOnAction(e -> navigateTo("dashboard", "/view/dashboard.fxml", btnDashboard));
        btnUsers.setOnAction(e -> navigateTo("users", "/view/user_mgmt.fxml", btnUsers));
        btnAudit.setOnAction(e -> navigateTo("audit", "/view/audit.fxml", btnAudit));

        // ── Toggle Utilisateurs visibility ──────────────────────────────────
        btnToggleUsers.setOnAction(e -> {
            usersVisible = !usersVisible;
            btnUsers.setVisible(usersVisible);
            btnUsers.setManaged(usersVisible);
            // ◁ = visible (click to hide), ▷ = hidden (click to show)
            btnToggleUsers.setText(usersVisible ? "◁" : "▷");
            btnToggleUsers.setStyle(usersVisible ? "" : "-fx-text-fill:#6366f1;");
            // If the user was on the Users page while we hide it, go to Dashboard
            if (!usersVisible && btnUsers.getStyleClass().contains("nav-active")) {
                navigateTo("dashboard", "/view/dashboard.fxml", btnDashboard);
            }
        });

        btnTheme.setOnAction(e -> {
            AppContext.toggleTheme(rootStack);
            btnTheme.setText(AppContext.isDark(rootStack) ? "☀ Clair" : "☾ Sombre");
        });

        btnLogout.setOnAction(e -> {
            AppContext.unregister(rootStack);
            Stage stage = (Stage) rootStack.getScene().getWindow();
            stage.close();
            LoginController.openLogin("admin", "GestNotes — Connexion Admin");
        });

        navigateTo("dashboard", "/view/dashboard.fxml", btnDashboard);
    }

    private void navigateTo(String key, String fxml, Button btn) {
        if (activeButton != null) activeButton.getStyleClass().remove("nav-active");
        btn.getStyleClass().add("nav-active");
        activeButton = btn;

        ViewEntry entry = viewCache.computeIfAbsent(key, k -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                Node node = loader.load();
                return new ViewEntry(node, loader.getController());
            } catch (Exception ex) {
                AppLog.error("Load " + fxml, ex);
                return new ViewEntry(new Label("Erreur: " + fxml), null);
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
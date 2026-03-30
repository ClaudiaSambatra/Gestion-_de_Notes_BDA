package controller;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
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
    private Label lblUser;
    @FXML
    private Button btnDashboard, btnAudit, btnTheme;

    private record ViewEntry(Node node, Object controller) {
    }

    private final Map<String, ViewEntry> viewCache = new HashMap<>();
    private Button activeButton;

    @FXML
    public void initialize() {
        AppContext.init(rootStack, toastContainer);
        lblUser.setText(SessionManager.getUtilisateur());

        btnDashboard.setOnAction(e -> navigateTo("dashboard", "/view/dashboard.fxml", btnDashboard));
        btnAudit.setOnAction(e -> navigateTo("audit", "/view/audit.fxml", btnAudit));

        btnTheme.setOnAction(e -> {
            AppContext.toggleTheme();
            btnTheme.setText(AppContext.isDarkMode() ? "☀ Clair" : "☾ Sombre");
        });

        navigateTo("dashboard", "/view/dashboard.fxml", btnDashboard);
    }

    private void navigateTo(String key, String fxmlPath, Button btn) {
        if (activeButton != null) activeButton.getStyleClass().remove("nav-active");
        btn.getStyleClass().add("nav-active");
        activeButton = btn;

        ViewEntry entry = viewCache.computeIfAbsent(key, k -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
                Node node = loader.load();
                return new ViewEntry(node, loader.getController());
            } catch (Exception ex) {
                AppLog.error("Chargement " + fxmlPath, ex);
                return new ViewEntry(new Label("Erreur: " + fxmlPath), null);
            }
        });

        if (entry.controller() instanceof Refreshable r) {
            r.refreshData();
        }

        Node view = entry.node();
        if (!contentArea.getChildren().isEmpty()) {
            Node current = contentArea.getChildren().get(0);
            FadeTransition out = new FadeTransition(Duration.millis(80), current);
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
        FadeTransition ft = new FadeTransition(Duration.millis(150), n);
        ft.setToValue(1);
        ft.play();
    }
}

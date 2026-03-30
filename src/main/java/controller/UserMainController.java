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


public class UserMainController {

    @FXML
    private StackPane rootStack;
    @FXML
    private VBox toastContainer;
    @FXML
    private StackPane contentArea;
    @FXML
    private Label lblUser;
    @FXML
    private Button btnDashboard, btnEtudiants, btnMatieres, btnNotes, btnTheme;

    private record ViewEntry(Node node, Object controller) {
    }

    private final Map<String, ViewEntry> cache = new HashMap<>();
    private Button activeButton;

    @FXML
    public void initialize() {
        AppContext.init(rootStack, toastContainer);
        lblUser.setText(SessionManager.getUtilisateur());

        btnDashboard.setOnAction(e -> navigateTo("dashboard", "/view/dashboard.fxml", btnDashboard));
        btnEtudiants.setOnAction(e -> navigateTo("etudiant", "/view/etudiant.fxml", btnEtudiants));
        btnMatieres.setOnAction(e -> navigateTo("matiere", "/view/matiere.fxml", btnMatieres));
        btnNotes.setOnAction(e -> navigateTo("note", "/view/note.fxml", btnNotes));

        btnTheme.setOnAction(e -> {
            AppContext.toggleTheme();
            btnTheme.setText(AppContext.isDarkMode() ? "☀ Clair" : "☾ Sombre");
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
                return new ViewEntry(loader.load(), loader.getController());
            } catch (Exception ex) {
                AppLog.error("Chargement " + fxml, ex);
                return new ViewEntry(new Label("Erreur"), null);
            }
        });

        if (entry.controller() instanceof Refreshable r) r.refreshData();

        Node view = entry.node();
        if (!contentArea.getChildren().isEmpty()) {
            Node cur = contentArea.getChildren().get(0);
            FadeTransition fadeOut = new FadeTransition(Duration.millis(80), cur);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(e -> {
                contentArea.getChildren().setAll(view);
                view.setOpacity(0);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(150), view);
                fadeIn.setToValue(1);
                fadeIn.play();
            });
            fadeOut.play();
        } else {
            contentArea.getChildren().setAll(view);
            view.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(150), view);
            ft.setToValue(1);
            ft.play();
        }
    }
}

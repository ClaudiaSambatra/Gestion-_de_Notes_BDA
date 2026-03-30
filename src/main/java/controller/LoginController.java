package controller;

import dao.UtilisateurDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Utilisateur;
import util.AppLog;

import java.util.Objects;

public class LoginController {

    @FXML
    private VBox loginRoot;
    @FXML
    private Label lblTitle, lblSubtitle, lblError;
    @FXML
    private TextField txtLogin;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private Button btnLogin;

    private String loginType = "admin"; // "admin" or "user"

    public void setLoginType(String type) {
        this.loginType = type;
        if ("admin".equals(type)) {
            lblTitle.setText("Administration");
            lblSubtitle.setText("Connectez-vous en tant qu'administrateur");
        } else {
            lblTitle.setText("Espace Utilisateur");
            lblSubtitle.setText("Enseignant ou Etudiant");
        }
    }

    @FXML
    public void initialize() {
        lblError.setVisible(false);
        // Enter key submits
        txtPassword.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) handleLogin();
        });
        txtLogin.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) txtPassword.requestFocus();
        });
    }

    @FXML
    private void handleLogin() {
        lblError.setVisible(false);
        String login = txtLogin.getText();
        String password = txtPassword.getText();

        if (login == null || login.isBlank() || password == null || password.isBlank()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        btnLogin.setDisable(true);
        btnLogin.setText("Connexion...");

        // Run auth on background thread to avoid UI freeze
        javafx.concurrent.Task<Utilisateur> task = new javafx.concurrent.Task<>() {
            @Override
            protected Utilisateur call() {
                return UtilisateurDAO.authenticate(login.trim(), password);
            }
        };

        task.setOnSucceeded(e -> {
            btnLogin.setDisable(false);
            btnLogin.setText("Se connecter");
            Utilisateur user = task.getValue();
            if (user == null) {
                showError("Identifiants incorrects ou compte desactive.");
                txtPassword.clear();
                txtPassword.requestFocus();
                return;
            }
            // Check role matches login type
            if ("admin".equals(loginType) && !"ADMIN".equals(user.getRole())) {
                showError("Acces reserve aux administrateurs.");
                return;
            }
            if ("user".equals(loginType) && "ADMIN".equals(user.getRole())) {
                showError("Utilisez la fenetre Administration.");
                return;
            }
            // Success — open main window
            Stage loginStage = (Stage) loginRoot.getScene().getWindow();
            loginStage.close();
            openMainWindow(user);
        });

        task.setOnFailed(e -> {
            btnLogin.setDisable(false);
            btnLogin.setText("Se connecter");
            showError("Erreur de connexion a la base.");
            AppLog.error("Login failed", task.getException());
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setVisible(true);
    }

    private void openMainWindow(Utilisateur user) {
        try {
            String fxml, title;
            if ("ADMIN".equals(user.getRole())) {
                fxml = "/view/admin_main.fxml";
                title = "GestNotes — Administration";
            } else {
                fxml = "/view/user_main.fxml";
                title = "GestNotes — " + user.getNomComplet();
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Scene scene = new Scene(loader.load(),
                    "ADMIN".equals(user.getRole()) ? 1200 : 1000,
                    "ADMIN".equals(user.getRole()) ? 780 : 680);
            scene.getStylesheets().add(
                    Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

            // Pass logged user to main controller
            Object ctrl = loader.getController();
            if (ctrl instanceof AdminMainController amc) amc.setLoggedUser(user);
            if (ctrl instanceof UserMainController umc) umc.setLoggedUser(user);

            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.setScene(scene);
            stage.show();

            AppLog.info("Login: " + user.getLogin() + " (" + user.getRole() + ")");
        } catch (Exception e) {
            AppLog.error("openMainWindow", e);
        }
    }

    /**
     * Static method to open a login window (called by Main and by logout)
     */
    public static void openLogin(String loginType, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/view/login.fxml"));
            Scene scene = new Scene(loader.load(), 420, 520);
            scene.getStylesheets().add(
                    Objects.requireNonNull(LoginController.class.getResource("/css/style.css")).toExternalForm());
            LoginController ctrl = loader.getController();
            ctrl.setLoginType(loginType);
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setResizable(false);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            AppLog.error("openLogin", e);
        }
    }
}

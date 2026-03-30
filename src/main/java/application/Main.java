package application;

import controller.LoginController;
import javafx.application.Application;
import javafx.stage.Stage;
import util.DatabaseConnection;


public class Main extends Application {

    @Override
    public void init() throws Exception {
        DatabaseConnection.getConnection().close();
    }

    @Override
    public void start(Stage ignored) throws Exception {
        LoginController.openLogin("admin", "GestNotes — Connexion Admin");
        LoginController.openLogin("user", "GestNotes — Connexion Utilisateur");
    }

    public static void main(String[] args) {
        launch(args);
    }
}

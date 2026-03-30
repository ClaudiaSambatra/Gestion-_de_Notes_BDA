package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.DatabaseConnection;
import util.SessionManager;

import java.util.Objects;

public class UserMain extends Application {
    @Override
    public void init() throws Exception {
        SessionManager.setRole("USER");
        SessionManager.setUtilisateur("enseignant");
        DatabaseConnection.getConnection().close();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/user_main.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 680);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        stage.setTitle("GestNotes — Saisie des Notes");
        stage.setMinWidth(800);
        stage.setMinHeight(550);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

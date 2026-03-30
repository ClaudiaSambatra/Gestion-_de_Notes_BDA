package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.DatabaseConnection;
import util.SessionManager;

import java.util.Objects;

public class Main extends Application {
    @Override
    public void init() throws Exception {
        SessionManager.setRole("ADMIN");
        SessionManager.setUtilisateur("admin");
        DatabaseConnection.getConnection().close();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/admin_main.fxml"));
        Scene scene = new Scene(loader.load(), 1200, 780);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());
        stage.setTitle("GestNotes — Administration");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

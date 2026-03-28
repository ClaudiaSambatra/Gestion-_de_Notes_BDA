package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/main.fxml"));
        Scene scene = new Scene(loader.load(), 900, 650);
        scene.getStylesheets().add(Main.class.getResource("/style.css").toExternalForm());
        stage.setTitle("Gestion des Notes — Système d'Audit");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

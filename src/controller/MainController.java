package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.control.Label;
import util.SessionManager;

public class MainController {

    @FXML private TabPane tabPane;
    @FXML private Label lblUtilisateur;

    @FXML
    public void initialize() {
        lblUtilisateur.setText("Utilisateur : " + SessionManager.getUtilisateur());
        loadTabs();
    }

    private void loadTabs() {
        String[][] tabs = {
            {"Étudiants",  "/etudiant.fxml"},
            {"Matières",   "/matiere.fxml"},
            {"Notes",      "/note.fxml"},
            {"Audit",      "/audit.fxml"}
        };

        for (String[] t : tabs) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(t[1]));
                AnchorPane pane = loader.load();
                Tab tab = new Tab(t[0]);
                tab.setContent(pane);
                tab.setClosable(false);
                tabPane.getTabs().add(tab);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

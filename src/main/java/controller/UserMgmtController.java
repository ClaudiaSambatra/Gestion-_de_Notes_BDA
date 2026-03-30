package controller;

import dao.UtilisateurDAO;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Utilisateur;
import util.*;

import java.util.ArrayList;
import java.util.List;

public class UserMgmtController implements Refreshable {
    @FXML
    private TextField txtSearch;
    @FXML
    private StackPane tablePane;
    @FXML
    private TableView<Utilisateur> table;
    @FXML
    private TableColumn<Utilisateur, Integer> colId;
    @FXML
    private TableColumn<Utilisateur, String> colLogin, colNom, colPrenom, colRole;
    @FXML
    private TableColumn<Utilisateur, Boolean> colActif;
    @FXML
    private TableColumn<Utilisateur, Void> colActions;
    @FXML
    private Label lblCount, lblPage;
    @FXML
    private Button btnPrev, btnNext;
    private List<Utilisateur> allData = new ArrayList<>(), filtered = new ArrayList<>();
    private int page = 0;
    private static final int PS = 12;
    private Debouncer deb;

    @FXML
    public void initialize() {
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colId.setMaxWidth(1f * Integer.MAX_VALUE * 6);
        colId.setStyle("-fx-alignment:CENTER;");
        colLogin.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLogin()));
        colLogin.setMaxWidth(1f * Integer.MAX_VALUE * 15);
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colNom.setMaxWidth(1f * Integer.MAX_VALUE * 18);
        colPrenom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPrenom()));
        colPrenom.setMaxWidth(1f * Integer.MAX_VALUE * 18);
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole()));
        colRole.setMaxWidth(1f * Integer.MAX_VALUE * 14);
        colRole.setStyle("-fx-alignment:CENTER;");
        colRole.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String it, boolean em) {
                super.updateItem(it, em);
                if (em || it == null) {
                    setGraphic(null);
                    return;
                }
                Label b = new Label(it);
                b.getStyleClass().addAll("badge", "badge-role-" + it.toLowerCase());
                setGraphic(b);
                setAlignment(Pos.CENTER);
            }
        });
        colActif.setCellValueFactory(c -> new SimpleBooleanProperty(c.getValue().isActif()).asObject());
        colActif.setMaxWidth(1f * Integer.MAX_VALUE * 10);
        colActif.setStyle("-fx-alignment:CENTER;");
        colActif.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean it, boolean em) {
                super.updateItem(it, em);
                setAlignment(Pos.CENTER);
                if (em || it == null) {
                    setText(null);
                    return;
                }
                setText(it ? "✓ Actif" : "✗ Inactif");
                setStyle("-fx-alignment:CENTER;" + (it ? "-fx-text-fill:#22c55e;" : "-fx-text-fill:#ef4444;"));
            }
        });
        colActions.setMaxWidth(1f * Integer.MAX_VALUE * 14);
        colActions.setStyle("-fx-alignment:CENTER;");
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button be = new Button("\u270E"), bd = new Button("\u2715"), bk = new Button("\u26BF");
            private final HBox bx = new HBox(4, be, bk, bd);

            {
                be.getStyleClass().addAll("btn-icon", "btn-icon-edit");
                bd.getStyleClass().addAll("btn-icon", "btn-icon-delete");
                bk.getStyleClass().addAll("btn-icon", "btn-icon-edit");
                bk.setTooltip(new Tooltip("Changer mot de passe"));
                bx.setAlignment(Pos.CENTER);
                be.setOnAction(e -> editModal(getTableView().getItems().get(getIndex())));
                bd.setOnAction(e -> delModal(getTableView().getItems().get(getIndex())));
                bk.setOnAction(e -> pwdModal(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void i, boolean em) {
                super.updateItem(i, em);
                setGraphic(em ? null : bx);
            }
        });
        deb = new Debouncer(Duration.millis(300), () -> {
            page = 0;
            filter();
        });
        txtSearch.textProperty().addListener((o, a, v) -> deb.trigger());
        refreshData();
    }

    @FXML
    private void handleAdd() {
        TextField tLogin = new TextField();
        tLogin.setPromptText("login unique");
        TextField tNom = new TextField();
        tNom.setPromptText("Nom");
        TextField tPrenom = new TextField();
        tPrenom.setPromptText("Prenom");
        PasswordField tPwd = new PasswordField();
        tPwd.setPromptText("Mot de passe");
        ComboBox<String> cRole = new ComboBox<>(FXCollections.observableArrayList("ADMIN", "ENSEIGNANT", "ETUDIANT"));
        cRole.setPromptText("-- Role --");
        var f = FxHelper.buildForm(FxHelper.labeledField("Login", tLogin), FxHelper.labeledField("Nom", tNom), FxHelper.labeledField("Prenom", tPrenom),
                FxHelper.labeledField("Mot de passe", tPwd), FxHelper.labeledCombo("Role", cRole));
        ModalHelper.show("Nouvel Utilisateur", f, () -> {
            if (tLogin.getText() == null || tLogin.getText().isBlank()) {
                Toast.error("Login obligatoire.");
                return false;
            }
            if (tNom.getText() == null || tNom.getText().isBlank()) {
                Toast.error("Nom obligatoire.");
                return false;
            }
            if (tPwd.getText() == null || tPwd.getText().length() < 3) {
                Toast.error("Mot de passe (3 car. min).");
                return false;
            }
            if (cRole.getValue() == null) {
                Toast.error("Selectionnez un role.");
                return false;
            }
            Utilisateur u = new Utilisateur(0, tLogin.getText().trim(), tPwd.getText(), tNom.getText().trim(), tPrenom.getText() != null ? tPrenom.getText().trim() : "", cRole.getValue(), true);
            if (UtilisateurDAO.insert(u)) {
                Toast.success("Utilisateur cree.");
                refreshData();
                return true;
            }
            Toast.error("Erreur (login existant ?).");
            return false;
        });
    }

    private void editModal(Utilisateur u) {
        TextField tNom = new TextField(u.getNom()), tPrenom = new TextField(u.getPrenom());
        ComboBox<String> cRole = new ComboBox<>(FXCollections.observableArrayList("ADMIN", "ENSEIGNANT", "ETUDIANT"));
        cRole.setValue(u.getRole());
        CheckBox cbActif = new CheckBox("Compte actif");
        cbActif.setSelected(u.isActif());
        cbActif.getStyleClass().add("form-label");
        Label lbl = new Label("Login: " + u.getLogin() + " (ID #" + u.getId() + ")");
        lbl.getStyleClass().add("form-label");
        VBox actifBox = new VBox(4, cbActif);
        var f = FxHelper.buildForm(new VBox(4, lbl), FxHelper.labeledField("Nom", tNom), FxHelper.labeledField("Prenom", tPrenom), FxHelper.labeledCombo("Role", cRole), actifBox);
        ModalHelper.show("Modifier Utilisateur", f, () -> {
            if (tNom.getText() == null || tNom.getText().isBlank()) {
                Toast.error("Nom obligatoire.");
                return false;
            }
            if (cRole.getValue() == null) {
                Toast.error("Role obligatoire.");
                return false;
            }
            Utilisateur up = new Utilisateur(u.getId(), u.getLogin(), u.getMotDePasse(), tNom.getText().trim(), tPrenom.getText() != null ? tPrenom.getText().trim() : "", cRole.getValue(), cbActif.isSelected());
            if (UtilisateurDAO.update(up)) {
                Toast.success("Utilisateur modifie.");
                refreshData();
                return true;
            }
            Toast.error("Erreur.");
            return false;
        });
    }

    private void pwdModal(Utilisateur u) {
        PasswordField p = new PasswordField();
        p.setPromptText("Nouveau mot de passe");
        Label lbl = new Label("Changer le mot de passe de: " + u.getLogin());
        lbl.getStyleClass().add("form-label");
        var f = FxHelper.buildForm(new VBox(4, lbl), FxHelper.labeledField("Nouveau mot de passe", p));
        ModalHelper.show("Mot de passe", f, () -> {
            if (p.getText() == null || p.getText().length() < 3) {
                Toast.error("3 caracteres minimum.");
                return false;
            }
            if (UtilisateurDAO.updatePassword(u.getId(), p.getText())) {
                Toast.success("Mot de passe change.");
                return true;
            }
            Toast.error("Erreur.");
            return false;
        });
    }

    private void delModal(Utilisateur u) {
        ModalHelper.confirm("Supprimer", "Supprimer l'utilisateur \"" + u.getLogin() + "\" ?", "Supprimer", "btn-modal-danger", () -> {
            if (UtilisateurDAO.delete(u.getId())) {
                Toast.success("Utilisateur supprime.");
                refreshData();
            } else Toast.error("Erreur.");
        });
    }

    @FXML
    private void handleRefresh() {
        refreshData();
        Toast.info("Actualise.");
    }

    @FXML
    private void handlePrev() {
        if (page > 0) {
            page--;
            upd();
        }
    }

    @FXML
    private void handleNext() {
        if ((page + 1) * PS < filtered.size()) {
            page++;
            upd();
        }
    }

    @Override
    public void refreshData() {
        DataService.loadAsync(UtilisateurDAO::getAll, d -> {
            allData = d;
            page = 0;
            filter();
        }, tablePane);
    }

    private void filter() {
        String k = txtSearch.getText() == null ? "" : txtSearch.getText().toLowerCase().trim();
        filtered = k.isEmpty() ? new ArrayList<>(allData) : allData.stream().filter(u -> u.getLogin().toLowerCase().contains(k) || u.getNom().toLowerCase().contains(k) || (u.getPrenom() != null && u.getPrenom().toLowerCase().contains(k)) || u.getRole().toLowerCase().contains(k)).toList();
        upd();
    }

    private void upd() {
        int tp = Math.max(1, (int) Math.ceil((double) filtered.size() / PS));
        page = Math.min(page, tp - 1);
        int f = page * PS, t = Math.min(f + PS, filtered.size());
        table.setItems(FXCollections.observableArrayList(filtered.subList(f, t)));
        lblCount.setText(filtered.size() + " utilisateur(s)");
        lblPage.setText("Page " + (page + 1) + " / " + tp);
        btnPrev.setDisable(page <= 0);
        btnNext.setDisable(page >= tp - 1);
    }
}

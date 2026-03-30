package util;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

public final class ModalHelper {
    private ModalHelper() {
    }

    public static void show(String title, Node content, java.util.function.BooleanSupplier onSave) {
        StackPane root = AppContext.currentRoot();
        if (root == null) return;
        Region bk = new Region();
        bk.getStyleClass().add("modal-backdrop");
        VBox card = new VBox(12);
        card.getStyleClass().add("modal-card");
        card.setPadding(new Insets(22, 26, 18, 26));
        card.setMaxWidth(440);
        card.setMinWidth(340);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        HBox hd = new HBox();
        hd.setAlignment(Pos.CENTER_LEFT);
        Label lt = new Label(title);
        lt.getStyleClass().add("modal-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button x = new Button("✕");
        x.getStyleClass().add("modal-close-btn");
        hd.getChildren().addAll(lt, sp, x);
        HBox ft = new HBox(10);
        ft.setAlignment(Pos.CENTER_RIGHT);
        ft.setPadding(new Insets(4, 0, 0, 0));
        Button ca = new Button("Annuler");
        ca.getStyleClass().addAll("button", "btn-modal-cancel");
        Button sv = new Button("Enregistrer");
        sv.getStyleClass().addAll("button", "btn-modal-save");
        ft.getChildren().addAll(ca, sv);
        card.getChildren().addAll(hd, content, ft);
        StackPane ov = new StackPane(bk, card);
        ov.getStyleClass().add("modal-overlay");
        StackPane.setAlignment(card, Pos.CENTER);
        Runnable cl = () -> animOut(ov, root);
        bk.setOnMouseClicked(e -> cl.run());
        x.setOnAction(e -> cl.run());
        ca.setOnAction(e -> cl.run());
        sv.setOnAction(e -> {
            if (onSave.getAsBoolean()) cl.run();
        });
        card.setScaleX(0.9);
        card.setScaleY(0.9);
        card.setOpacity(0);
        bk.setOpacity(0);
        root.getChildren().add(ov);
        animIn(bk, card);
    }

    public static void confirm(String title, String msg, String actionLabel, String actionStyle, Runnable onConfirm) {
        StackPane root = AppContext.currentRoot();
        if (root == null) return;
        Region bk = new Region();
        bk.getStyleClass().add("modal-backdrop");
        VBox card = new VBox(14);
        card.getStyleClass().add("modal-card");
        card.setPadding(new Insets(22, 26, 18, 26));
        card.setMaxWidth(400);
        card.setMinWidth(300);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        Label lt = new Label(title);
        lt.getStyleClass().add("modal-title");
        Label lm = new Label(msg);
        lm.getStyleClass().add("modal-message");
        lm.setWrapText(true);
        HBox ft = new HBox(10);
        ft.setAlignment(Pos.CENTER_RIGHT);
        ft.setPadding(new Insets(4, 0, 0, 0));
        Button ca = new Button("Annuler");
        ca.getStyleClass().addAll("button", "btn-modal-cancel");
        Button ac = new Button(actionLabel);
        ac.getStyleClass().addAll("button", actionStyle);
        ft.getChildren().addAll(ca, ac);
        card.getChildren().addAll(lt, lm, ft);
        StackPane ov = new StackPane(bk, card);
        ov.getStyleClass().add("modal-overlay");
        StackPane.setAlignment(card, Pos.CENTER);
        Runnable cl = () -> animOut(ov, root);
        bk.setOnMouseClicked(e -> cl.run());
        ca.setOnAction(e -> cl.run());
        ac.setOnAction(e -> {
            onConfirm.run();
            cl.run();
        });
        card.setScaleX(0.9);
        card.setScaleY(0.9);
        card.setOpacity(0);
        bk.setOpacity(0);
        root.getChildren().add(ov);
        animIn(bk, card);
    }

    private static void animIn(Region bk, VBox card) {
        FadeTransition bf = new FadeTransition(Duration.millis(180), bk);
        bf.setToValue(1);
        ScaleTransition sc = new ScaleTransition(Duration.millis(250), card);
        sc.setToX(1);
        sc.setToY(1);
        sc.setInterpolator(Interpolator.SPLINE(0.16, 1, 0.3, 1));
        FadeTransition cf = new FadeTransition(Duration.millis(180), card);
        cf.setToValue(1);
        new ParallelTransition(bf, sc, cf).play();
    }

    private static void animOut(StackPane ov, StackPane root) {
        FadeTransition f = new FadeTransition(Duration.millis(180), ov);
        f.setToValue(0);
        f.setOnFinished(e -> root.getChildren().remove(ov));
        f.play();
    }
}

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
        StackPane root = AppContext.getRootStack();
        if (root == null) return;

        Region backdrop = new Region();
        backdrop.getStyleClass().add("modal-backdrop");

        VBox card = new VBox(12);
        card.getStyleClass().add("modal-card");
        card.setPadding(new Insets(22, 26, 18, 26));
        card.setMaxWidth(440);
        card.setMinWidth(340);
        card.setMaxHeight(Region.USE_PREF_SIZE);

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label t = new Label(title);
        t.getStyleClass().add("modal-title");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button x = new Button("✕");
        x.getStyleClass().add("modal-close-btn");
        header.getChildren().addAll(t, sp, x);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(4, 0, 0, 0));
        Button cancel = new Button("Annuler");
        cancel.getStyleClass().addAll("button", "btn-modal-cancel");
        Button save = new Button("Enregistrer");
        save.getStyleClass().addAll("button", "btn-modal-save");
        footer.getChildren().addAll(cancel, save);

        card.getChildren().addAll(header, content, footer);
        StackPane overlay = new StackPane(backdrop, card);
        overlay.getStyleClass().add("modal-overlay");
        StackPane.setAlignment(card, Pos.CENTER);

        Runnable close = () -> animateOut(overlay, root);
        backdrop.setOnMouseClicked(e -> close.run());
        x.setOnAction(e -> close.run());
        cancel.setOnAction(e -> close.run());
        save.setOnAction(e -> {
            if (onSave.getAsBoolean()) close.run();
        });

        card.setScaleX(0.9);
        card.setScaleY(0.9);
        card.setOpacity(0);
        backdrop.setOpacity(0);
        root.getChildren().add(overlay);
        animateIn(backdrop, card);
    }

    public static void confirm(String title, String msg, String actionLabel, String actionStyle, Runnable onConfirm) {
        StackPane root = AppContext.getRootStack();
        if (root == null) return;

        Region backdrop = new Region();
        backdrop.getStyleClass().add("modal-backdrop");
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

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(4, 0, 0, 0));
        Button cancel = new Button("Annuler");
        cancel.getStyleClass().addAll("button", "btn-modal-cancel");
        Button action = new Button(actionLabel);
        action.getStyleClass().addAll("button", actionStyle);
        footer.getChildren().addAll(cancel, action);
        card.getChildren().addAll(lt, lm, footer);

        StackPane overlay = new StackPane(backdrop, card);
        overlay.getStyleClass().add("modal-overlay");
        StackPane.setAlignment(card, Pos.CENTER);

        Runnable close = () -> animateOut(overlay, root);
        backdrop.setOnMouseClicked(e -> close.run());
        cancel.setOnAction(e -> close.run());
        action.setOnAction(e -> {
            onConfirm.run();
            close.run();
        });

        card.setScaleX(0.9);
        card.setScaleY(0.9);
        card.setOpacity(0);
        backdrop.setOpacity(0);
        root.getChildren().add(overlay);
        animateIn(backdrop, card);
    }

    private static void animateIn(Region backdrop, VBox card) {
        FadeTransition bf = new FadeTransition(Duration.millis(180), backdrop);
        bf.setToValue(1);
        ScaleTransition sc = new ScaleTransition(Duration.millis(250), card);
        sc.setToX(1);
        sc.setToY(1);
        sc.setInterpolator(Interpolator.SPLINE(0.16, 1, 0.3, 1));
        FadeTransition cf = new FadeTransition(Duration.millis(180), card);
        cf.setToValue(1);
        new ParallelTransition(bf, sc, cf).play();
    }

    private static void animateOut(StackPane overlay, StackPane root) {
        FadeTransition ft = new FadeTransition(Duration.millis(180), overlay);
        ft.setToValue(0);
        ft.setOnFinished(e -> root.getChildren().remove(overlay));
        ft.play();
    }
}

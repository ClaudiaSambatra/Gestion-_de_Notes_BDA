package util;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public final class Toast {
    private static final int MAX = 5;

    private Toast() {
    }

    public static void success(String m) {
        show(m, "toast-success", "✓");
    }

    public static void error(String m) {
        show(m, "toast-error", "✗");
    }

    public static void info(String m) {
        show(m, "toast-info", "ℹ");
    }

    private static void show(String msg, String style, String icon) {
        Platform.runLater(() -> {
            VBox container = AppContext.currentToasts();
            if (container == null) return;
            while (container.getChildren().size() >= MAX) container.getChildren().remove(0);
            HBox t = new HBox(8);
            t.setAlignment(Pos.CENTER_LEFT);
            t.getStyleClass().addAll("toast", style);
            t.setPadding(new Insets(10, 14, 10, 14));
            t.setMaxWidth(360);
            t.setMinWidth(260);
            t.setPickOnBounds(true);
            Label ic = new Label(icon);
            ic.getStyleClass().add("toast-icon");
            Label mg = new Label(msg);
            mg.getStyleClass().add("toast-message");
            mg.setWrapText(true);
            mg.setMaxWidth(230);
            HBox.setHgrow(mg, Priority.ALWAYS);
            Label cl = new Label("✕");
            cl.getStyleClass().add("toast-close");
            cl.setCursor(Cursor.HAND);
            t.getChildren().addAll(ic, mg, cl);
            t.setTranslateX(400);
            t.setOpacity(0);
            container.getChildren().add(t);
            TranslateTransition si = new TranslateTransition(Duration.millis(300), t);
            si.setFromX(400);
            si.setToX(0);
            si.setInterpolator(Interpolator.SPLINE(0.25, 0.1, 0.25, 1.0));
            FadeTransition fi = new FadeTransition(Duration.millis(300), t);
            fi.setFromValue(0);
            fi.setToValue(1);
            ParallelTransition en = new ParallelTransition(si, fi);
            PauseTransition pa = new PauseTransition(Duration.seconds(4));
            SequentialTransition au = new SequentialTransition(en, pa);
            au.setOnFinished(e -> dismiss(t, container));
            t.setOnMouseClicked(e -> {
                au.stop();
                dismiss(t, container);
            });
            cl.setOnMouseClicked(e -> {
                e.consume();
                au.stop();
                dismiss(t, container);
            });
            au.play();
        });
    }

    private static void dismiss(HBox t, VBox c) {
        if (!c.getChildren().contains(t)) return;
        FadeTransition fo = new FadeTransition(Duration.millis(250), t);
        fo.setToValue(0);
        TranslateTransition so = new TranslateTransition(Duration.millis(250), t);
        so.setToX(400);
        ParallelTransition ex = new ParallelTransition(fo, so);
        ex.setOnFinished(e -> c.getChildren().remove(t));
        ex.play();
    }
}

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
    private static final int MAX_VISIBLE = 5;

    private Toast() {
    }

    public static void success(String msg) {
        show(msg, "toast-success", "✓");
    }

    public static void error(String msg) {
        show(msg, "toast-error", "✗");
    }

    public static void info(String msg) {
        show(msg, "toast-info", "ℹ");
    }

    private static void show(String message, String style, String icon) {
        Platform.runLater(() -> {
            VBox container = AppContext.getToastContainer();
            if (container == null) return;
            while (container.getChildren().size() >= MAX_VISIBLE) container.getChildren().remove(0);

            HBox toast = new HBox(8);
            toast.setAlignment(Pos.CENTER_LEFT);
            toast.getStyleClass().addAll("toast", style);
            toast.setPadding(new Insets(10, 14, 10, 14));
            toast.setMaxWidth(360);
            toast.setMinWidth(260);
            toast.setPickOnBounds(true);

            Label ic = new Label(icon);
            ic.getStyleClass().add("toast-icon");
            Label mg = new Label(message);
            mg.getStyleClass().add("toast-message");
            mg.setWrapText(true);
            mg.setMaxWidth(230);
            HBox.setHgrow(mg, Priority.ALWAYS);
            Label cl = new Label("✕");
            cl.getStyleClass().add("toast-close");
            cl.setCursor(Cursor.HAND);

            toast.getChildren().addAll(ic, mg, cl);
            toast.setTranslateX(400);
            toast.setOpacity(0);
            container.getChildren().add(toast);

            TranslateTransition si = new TranslateTransition(Duration.millis(300), toast);
            si.setFromX(400);
            si.setToX(0);
            si.setInterpolator(Interpolator.SPLINE(0.25, 0.1, 0.25, 1.0));
            FadeTransition fi = new FadeTransition(Duration.millis(300), toast);
            fi.setFromValue(0);
            fi.setToValue(1);
            ParallelTransition enter = new ParallelTransition(si, fi);
            PauseTransition pause = new PauseTransition(Duration.seconds(4));
            SequentialTransition auto = new SequentialTransition(enter, pause);
            auto.setOnFinished(e -> dismiss(toast, container));

            toast.setOnMouseClicked(e -> {
                auto.stop();
                dismiss(toast, container);
            });
            cl.setOnMouseClicked(e -> {
                e.consume();
                auto.stop();
                dismiss(toast, container);
            });
            auto.play();
        });
    }

    private static void dismiss(HBox toast, VBox container) {
        if (!container.getChildren().contains(toast)) return;
        FadeTransition fo = new FadeTransition(Duration.millis(250), toast);
        fo.setToValue(0);
        TranslateTransition so = new TranslateTransition(Duration.millis(250), toast);
        so.setToX(400);
        ParallelTransition exit = new ParallelTransition(fo, so);
        exit.setOnFinished(e -> container.getChildren().remove(toast));
        exit.play();
    }
}

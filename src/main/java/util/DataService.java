package util;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class DataService {
    private DataService() {
    }

    public static <T> void loadAsync(Supplier<T> bg, Consumer<T> ok, Node target) {
        loadAsync(bg, ok, ex -> Toast.error("Erreur de chargement."), target);
    }

    public static <T> void loadAsync(Supplier<T> bg, Consumer<T> ok, Consumer<Throwable> err, Node target) {
        Node sp = null;
        if (target instanceof StackPane s) {
            sp = createSpinner();
            s.getChildren().add(sp);
        }
        final Node ref = sp;
        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return bg.get();
            }
        };
        task.setOnSucceeded(e -> {
            rmSpin(target, ref);
            ok.accept(task.getValue());
        });
        task.setOnFailed(e -> {
            rmSpin(target, ref);
            AppLog.error("DataService", task.getException());
            if (err != null) err.accept(task.getException());
        });
        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }

    private static Node createSpinner() {
        ProgressIndicator pi = new ProgressIndicator();
        pi.setMaxSize(36, 36);
        pi.getStyleClass().add("loading-spinner");
        VBox b = new VBox(pi);
        b.setAlignment(Pos.CENTER);
        b.getStyleClass().add("loading-overlay");
        b.setPickOnBounds(true);
        return b;
    }

    private static void rmSpin(Node t, Node s) {
        if (s != null && t instanceof StackPane sp) Platform.runLater(() -> sp.getChildren().remove(s));
    }
}

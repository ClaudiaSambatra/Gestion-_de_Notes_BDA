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


    public static <T> void loadAsync(Supplier<T> bgWork, Consumer<T> onSuccess, Consumer<Throwable> onError, Node target) {
        Node spinner = null;
        if (target instanceof StackPane sp) {
            spinner = createSpinner();
            sp.getChildren().add(spinner);
        }

        final Node spinnerRef = spinner;

        Task<T> task = new Task<>() {
            @Override
            protected T call() {
                return bgWork.get();
            }
        };

        task.setOnSucceeded(e -> {
            removeSpinner(target, spinnerRef);
            onSuccess.accept(task.getValue());
        });

        task.setOnFailed(e -> {
            removeSpinner(target, spinnerRef);
            Throwable ex = task.getException();
            AppLog.error("DataService async failed", ex);
            if (onError != null) onError.accept(ex);
        });

        Thread t = new Thread(task);
        t.setDaemon(true);
        t.start();
    }


    public static <T> void loadAsync(Supplier<T> bgWork, Consumer<T> onSuccess, Node target) {
        loadAsync(bgWork, onSuccess, ex -> Toast.error("Erreur de chargement."), target);
    }

    private static Node createSpinner() {
        ProgressIndicator pi = new ProgressIndicator();
        pi.setMaxSize(36, 36);
        pi.getStyleClass().add("loading-spinner");

        VBox box = new VBox(pi);
        box.setAlignment(Pos.CENTER);
        box.getStyleClass().add("loading-overlay");
        box.setPickOnBounds(true);
        return box;
    }

    private static void removeSpinner(Node target, Node spinner) {
        if (spinner != null && target instanceof StackPane sp) {
            Platform.runLater(() -> sp.getChildren().remove(spinner));
        }
    }
}

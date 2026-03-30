package util;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Multi-window context manager.
 * Each window registers its own rootStack + toastContainer.
 * Toast/Modal resolve the correct context via the focused window.
 */
public final class AppContext {
    private static final Map<StackPane, VBox> toastMap = new LinkedHashMap<>();

    private AppContext() {
    }

    public static void register(StackPane root, VBox toasts) {
        toastMap.put(root, toasts);
        toasts.setPickOnBounds(false);
    }

    public static void unregister(StackPane root) {
        toastMap.remove(root);
    }

    /**
     * Find the root StackPane of the currently focused window
     */
    public static StackPane currentRoot() {
        for (StackPane sp : toastMap.keySet()) {
            if (sp.getScene() != null && sp.getScene().getWindow() != null
                    && sp.getScene().getWindow().isFocused()) return sp;
        }
        // Fallback: first available
        return toastMap.keySet().stream().findFirst().orElse(null);
    }

    public static VBox currentToasts() {
        StackPane r = currentRoot();
        return r != null ? toastMap.get(r) : null;
    }

    public static void toggleTheme(StackPane root) {
        if (root.getStyleClass().contains("dark")) root.getStyleClass().remove("dark");
        else root.getStyleClass().add("dark");
    }

    public static boolean isDark(StackPane root) {
        return root.getStyleClass().contains("dark");
    }
}

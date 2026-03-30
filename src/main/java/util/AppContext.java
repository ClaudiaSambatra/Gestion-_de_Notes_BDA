package util;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AppContext {

    private static final Map<StackPane, VBox> toastContainers = new LinkedHashMap<>();
    private static final Map<StackPane, Boolean> darkModes = new LinkedHashMap<>();

    private static StackPane activeRoot;

    private AppContext() {
    }


    public static void init(StackPane root, VBox toasts) {
        toastContainers.put(root, toasts);
        darkModes.put(root, false);
        toasts.setPickOnBounds(false);

        root.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene == null) return;
            newScene.windowProperty().addListener((obsWin, oldWin, newWin) -> {
                if (newWin == null) return;
                newWin.focusedProperty().addListener((obsFocus, wasFocused, isFocused) -> {
                    if (isFocused) activeRoot = root;
                });
            });
        });

        activeRoot = root;
    }


    public static StackPane getRootStack() {
        return activeRoot;
    }

    public static VBox getToastContainer() {
        return activeRoot != null ? toastContainers.get(activeRoot) : null;
    }

    public static boolean isDarkMode() {
        return activeRoot != null && Boolean.TRUE.equals(darkModes.get(activeRoot));
    }

    public static void toggleTheme() {
        if (activeRoot == null) return;
        boolean nowDark = !Boolean.TRUE.equals(darkModes.get(activeRoot));
        darkModes.put(activeRoot, nowDark);
        if (nowDark) activeRoot.getStyleClass().add("dark");
        else activeRoot.getStyleClass().remove("dark");
    }
}
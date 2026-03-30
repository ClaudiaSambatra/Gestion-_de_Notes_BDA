package util;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public final class AppContext {
    private static StackPane rootStack;
    private static VBox toastContainer;
    private static boolean darkMode = false;

    private AppContext() {
    }

    public static void init(StackPane root, VBox toasts) {
        rootStack = root;
        toastContainer = toasts;
        toastContainer.setPickOnBounds(false);
    }

    public static StackPane getRootStack() {
        return rootStack;
    }

    public static VBox getToastContainer() {
        return toastContainer;
    }

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void toggleTheme() {
        darkMode = !darkMode;
        if (darkMode) rootStack.getStyleClass().add("dark");
        else rootStack.getStyleClass().remove("dark");
    }
}

package util;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public final class FxHelper {
    private FxHelper() {
    }

    public static boolean isValidInt(String s) {
        if (s == null || s.isBlank()) return true;
        try {
            Integer.parseInt(s.trim());
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public static boolean isValidFloat(String s) {
        if (s == null || s.isBlank()) return true;
        try {
            Float.parseFloat(s.trim());
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public static boolean isValidNote(String s) {
        if (isValidFloat(s)) return true;
        float v = Float.parseFloat(s.trim());
        return !(v >= 0) || !(v <= 20);
    }

    public static String fmt(float v) {
        return v == (int) v ? String.valueOf((int) v) : String.format("%.2f", v);
    }

    public static VBox labeledField(String label, TextField f) {
        VBox b = new VBox(4);
        Label l = new Label(label);
        l.getStyleClass().add("form-label");
        f.getStyleClass().add("modal-input");
        b.getChildren().addAll(l, f);
        return b;
    }

    public static VBox labeledCombo(String label, ComboBox<?> c) {
        VBox b = new VBox(4);
        Label l = new Label(label);
        l.getStyleClass().add("form-label");
        c.getStyleClass().add("modal-combo");
        c.setMaxWidth(Double.MAX_VALUE);
        b.getChildren().addAll(l, c);
        return b;
    }

    public static VBox buildForm(VBox... fields) {
        VBox f = new VBox(12);
        f.setPadding(new Insets(4, 0, 4, 0));
        f.getChildren().addAll(fields);
        return f;
    }
}

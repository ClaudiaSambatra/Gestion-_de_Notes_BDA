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

    public static VBox labeledField(String label, TextField field) {
        VBox box = new VBox(4);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("form-label");
        field.getStyleClass().add("modal-input");
        box.getChildren().addAll(lbl, field);
        return box;
    }

    public static VBox labeledCombo(String label, ComboBox<?> combo) {
        VBox box = new VBox(4);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("form-label");
        combo.getStyleClass().add("modal-combo");
        combo.setMaxWidth(Double.MAX_VALUE);
        box.getChildren().addAll(lbl, combo);
        return box;
    }

    public static VBox buildForm(VBox... fields) {
        VBox form = new VBox(12);
        form.setPadding(new Insets(4, 0, 4, 0));
        form.getChildren().addAll(fields);
        return form;
    }
}

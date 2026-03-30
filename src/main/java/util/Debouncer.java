package util;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

public final class Debouncer {
    private final PauseTransition p;

    public Debouncer(Duration delay, Runnable action) {
        p = new PauseTransition(delay);
        p.setOnFinished(e -> action.run());
    }

    public void trigger() {
        p.playFromStart();
    }
}

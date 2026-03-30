package util;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

public final class Debouncer {

    private final PauseTransition pause;

    public Debouncer(Duration delay, Runnable action) {
        pause = new PauseTransition(delay);
        pause.setOnFinished(e -> action.run());
    }

    public void trigger() {
        pause.playFromStart();
    }

    public void stop() {
        pause.stop();
    }
}

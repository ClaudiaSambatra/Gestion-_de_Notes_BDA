package util;

import java.util.logging.*;

public final class AppLog {
    private static final Logger L = Logger.getLogger("GestNotes");

    static {
        L.setUseParentHandlers(false);
        ConsoleHandler h = new ConsoleHandler();
        h.setLevel(Level.ALL);
        h.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord r) {
                String s = r.getSourceClassName();
                if (s != null) {
                    int d = s.lastIndexOf('.');
                    if (d >= 0) s = s.substring(d + 1);
                }
                StringBuilder b = new StringBuilder();
                b.append(String.format("[%-7s] %-20s %s%n", r.getLevel(), s, formatMessage(r)));
                if (r.getThrown() != null) {
                    Throwable t = r.getThrown();
                    b.append("  ").append(t.getClass().getSimpleName()).append(": ").append(t.getMessage()).append("\n");
                }
                return b.toString();
            }
        });
        L.addHandler(h);
        L.setLevel(Level.ALL);
    }

    private AppLog() {
    }

    public static void info(String m) {
        L.log(Level.INFO, m);
    }

    public static void warn(String m) {
        L.log(Level.WARNING, m);
    }

    public static void error(String m) {
        L.log(Level.SEVERE, m);
    }

    public static void error(String m, Throwable t) {
        L.log(Level.SEVERE, m, t);
    }
}

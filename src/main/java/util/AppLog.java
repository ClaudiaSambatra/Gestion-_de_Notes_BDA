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
                String src = r.getSourceClassName();
                if (src != null) {
                    int d = src.lastIndexOf('.');
                    if (d >= 0) src = src.substring(d + 1);
                }
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("[%-7s] %-20s %s%n", r.getLevel(), src, formatMessage(r)));
                if (r.getThrown() != null) {
                    Throwable t = r.getThrown();
                    sb.append("  ").append(t.getClass().getSimpleName()).append(": ").append(t.getMessage()).append("\n");
                    for (StackTraceElement el : t.getStackTrace()) {
                        String cn = el.getClassName();
                        if (cn.startsWith("dao.") || cn.startsWith("util.") || cn.startsWith("controller.") || cn.startsWith("application."))
                            sb.append("    at ").append(el).append("\n");
                    }
                }
                return sb.toString();
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

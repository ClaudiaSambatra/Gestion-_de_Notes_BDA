package util;

public class SessionManager {
    private static String utilisateur = "admin";
    private static String role = "ADMIN";

    public static String getUtilisateur() {
        return utilisateur;
    }

    public static void setUtilisateur(String u) {
        utilisateur = u;
    }

    public static String getRole() {
        return role;
    }

    public static void setRole(String r) {
        role = r;
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}

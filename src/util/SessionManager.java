package util;

public class SessionManager {
    private static String utilisateurConnecte = "admin";

    public static String getUtilisateur() {
        return utilisateurConnecte;
    }

    public static void setUtilisateur(String utilisateur) {
        utilisateurConnecte = utilisateur;
    }
}

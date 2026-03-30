package model;

public class Utilisateur {
    private int id;
    private String login, motDePasse, nom, prenom, role;
    private boolean actif;

    public Utilisateur() {
    }

    public Utilisateur(int id, String login, String mdp, String nom, String prenom, String role, boolean actif) {
        this.id = id;
        this.login = login;
        motDePasse = mdp;
        this.nom = nom;
        this.prenom = prenom;
        this.role = role;
        this.actif = actif;
    }

    public int getId() {
        return id;
    }

    public void setId(int i) {
        id = i;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String l) {
        login = l;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String m) {
        motDePasse = m;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String n) {
        nom = n;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String p) {
        prenom = p;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String r) {
        role = r;
    }

    public boolean isActif() {
        return actif;
    }

    public void setActif(boolean a) {
        actif = a;
    }

    public String getNomComplet() {
        return (prenom == null || prenom.isBlank()) ? nom : prenom + " " + nom;
    }

    public String getInitiales() {
        String p = prenom != null && !prenom.isBlank() ? prenom.substring(0, 1).toUpperCase() : "";
        String n = nom != null && !nom.isBlank() ? nom.substring(0, 1).toUpperCase() : "";
        return p + n;
    }

    @Override
    public String toString() {
        return login + " (" + role + ")";
    }
}

package model;

public class Etudiant {
    private int numEtudiant;
    private String nom;
    private float moyenne;

    public Etudiant() {
    }

    public Etudiant(int n, String nom, float m) {
        this.numEtudiant = n;
        this.nom = nom;
        this.moyenne = m;
    }

    public int getNumEtudiant() {
        return numEtudiant;
    }

    public String getNom() {
        return nom;
    }

    public float getMoyenne() {
        return moyenne;
    }

    public void setNumEtudiant(int n) {
        numEtudiant = n;
    }

    public void setNom(String n) {
        nom = n;
    }

    public void setMoyenne(float m) {
        moyenne = m;
    }

    @Override
    public String toString() {
        return numEtudiant + " - " + nom;
    }
}

package model;

public class Etudiant {
    private int numEtudiant;
    private String nom;
    private float moyenne;

    public Etudiant() {}

    public Etudiant(int numEtudiant, String nom, float moyenne) {
        this.numEtudiant = numEtudiant;
        this.nom = nom;
        this.moyenne = moyenne;
    }

    public int getNumEtudiant() { return numEtudiant; }
    public void setNumEtudiant(int numEtudiant) { this.numEtudiant = numEtudiant; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public float getMoyenne() { return moyenne; }
    public void setMoyenne(float moyenne) { this.moyenne = moyenne; }
}

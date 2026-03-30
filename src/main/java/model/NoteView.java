package model;

public class NoteView {
    private final int numEtudiant;
    private final String nomEtudiant;
    private final int numMat;
    private final String designMatiere;
    private final float note, coef;

    public NoteView(int ne, String nom, int nm, String des, float n, float c) {
        numEtudiant = ne;
        nomEtudiant = nom;
        numMat = nm;
        designMatiere = des;
        note = n;
        coef = c;
    }

    public int getNumEtudiant() {
        return numEtudiant;
    }

    public String getNomEtudiant() {
        return nomEtudiant;
    }

    public int getNumMat() {
        return numMat;
    }

    public String getDesignMatiere() {
        return designMatiere;
    }

    public float getNote() {
        return note;
    }

    public float getCoef() {
        return coef;
    }
}

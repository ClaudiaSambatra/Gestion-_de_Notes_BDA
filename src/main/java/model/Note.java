package model;

public class Note {
    private int numEtudiant, numMat;
    private float note;

    public Note() {
    }

    public Note(int ne, int nm, float n) {
        numEtudiant = ne;
        numMat = nm;
        note = n;
    }

    public int getNumEtudiant() {
        return numEtudiant;
    }

    public int getNumMat() {
        return numMat;
    }

    public float getNote() {
        return note;
    }

    public void setNumEtudiant(int n) {
        numEtudiant = n;
    }

    public void setNumMat(int n) {
        numMat = n;
    }

    public void setNote(float n) {
        note = n;
    }
}

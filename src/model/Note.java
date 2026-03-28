package model;

public class Note {
    private int numEtudiant;
    private int numMat;
    private float note;

    public Note() {}

    public Note(int numEtudiant, int numMat, float note) {
        this.numEtudiant = numEtudiant;
        this.numMat = numMat;
        this.note = note;
    }

    public int getNumEtudiant() { return numEtudiant; }
    public void setNumEtudiant(int numEtudiant) { this.numEtudiant = numEtudiant; }

    public int getNumMat() { return numMat; }
    public void setNumMat(int numMat) { this.numMat = numMat; }

    public float getNote() { return note; }
    public void setNote(float note) { this.note = note; }
}

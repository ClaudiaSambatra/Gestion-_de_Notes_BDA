package model;

public class Matiere {
    private int numMat;
    private String design;
    private float coef;

    public Matiere() {
    }

    public Matiere(int n, String d, float c) {
        numMat = n;
        design = d;
        coef = c;
    }

    public int getNumMat() {
        return numMat;
    }

    public String getDesign() {
        return design;
    }

    public float getCoef() {
        return coef;
    }

    public void setNumMat(int n) {
        numMat = n;
    }

    public void setDesign(String d) {
        design = d;
    }

    public void setCoef(float c) {
        coef = c;
    }

    @Override
    public String toString() {
        return numMat + " - " + design;
    }
}

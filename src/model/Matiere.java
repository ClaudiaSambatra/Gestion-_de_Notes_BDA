package model;

public class Matiere {
    private int numMat;
    private String design;
    private float coef;

    public Matiere() {}

    public Matiere(int numMat, String design, float coef) {
        this.numMat = numMat;
        this.design = design;
        this.coef = coef;
    }

    public int getNumMat() { return numMat; }
    public void setNumMat(int numMat) { this.numMat = numMat; }

    public String getDesign() { return design; }
    public void setDesign(String design) { this.design = design; }

    public float getCoef() { return coef; }
    public void setCoef(float coef) { this.coef = coef; }
}

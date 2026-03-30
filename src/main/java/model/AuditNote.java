package model;

import java.time.LocalDateTime;

public class AuditNote {
    private int id;
    private String typeOperation;
    private LocalDateTime dateOperation;
    private int numEtudiant;
    private String nom, design;
    private Float noteAncien, noteNouv;
    private String utilisateur;

    public AuditNote() {
    }

    public AuditNote(String t, LocalDateTime d, int ne, String nom, String des, Float a, Float n, String u) {
        typeOperation = t;
        dateOperation = d;
        numEtudiant = ne;
        this.nom = nom;
        design = des;
        noteAncien = a;
        noteNouv = n;
        utilisateur = u;
    }

    public int getId() {
        return id;
    }

    public void setId(int i) {
        id = i;
    }

    public String getTypeOperation() {
        return typeOperation;
    }

    public LocalDateTime getDateOperation() {
        return dateOperation;
    }

    public int getNumEtudiant() {
        return numEtudiant;
    }

    public String getNom() {
        return nom;
    }

    public String getDesign() {
        return design;
    }

    public Float getNoteAncien() {
        return noteAncien;
    }

    public Float getNoteNouv() {
        return noteNouv;
    }

    public String getUtilisateur() {
        return utilisateur;
    }
}

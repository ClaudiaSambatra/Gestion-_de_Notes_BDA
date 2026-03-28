package model;

import java.time.LocalDateTime;

public class AuditNote {
    private int id;
    private String typeOperation;
    private LocalDateTime dateOperation;
    private int numEtudiant;
    private String nom;
    private String design;
    private Float noteAncien;
    private Float noteNouv;
    private String utilisateur;

    public AuditNote() {}

    public AuditNote(String typeOperation, LocalDateTime dateOperation, int numEtudiant,
                     String nom, String design, Float noteAncien, Float noteNouv, String utilisateur) {
        this.typeOperation = typeOperation;
        this.dateOperation = dateOperation;
        this.numEtudiant = numEtudiant;
        this.nom = nom;
        this.design = design;
        this.noteAncien = noteAncien;
        this.noteNouv = noteNouv;
        this.utilisateur = utilisateur;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTypeOperation() { return typeOperation; }
    public void setTypeOperation(String typeOperation) { this.typeOperation = typeOperation; }

    public LocalDateTime getDateOperation() { return dateOperation; }
    public void setDateOperation(LocalDateTime dateOperation) { this.dateOperation = dateOperation; }

    public int getNumEtudiant() { return numEtudiant; }
    public void setNumEtudiant(int numEtudiant) { this.numEtudiant = numEtudiant; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDesign() { return design; }
    public void setDesign(String design) { this.design = design; }

    public Float getNoteAncien() { return noteAncien; }
    public void setNoteAncien(Float noteAncien) { this.noteAncien = noteAncien; }

    public Float getNoteNouv() { return noteNouv; }
    public void setNoteNouv(Float noteNouv) { this.noteNouv = noteNouv; }

    public String getUtilisateur() { return utilisateur; }
    public void setUtilisateur(String utilisateur) { this.utilisateur = utilisateur; }
}

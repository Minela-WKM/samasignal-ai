package dev.samasignal.api.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "journaux_extraction")
public class JournalExtraction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String modele;

    @Column(columnDefinition = "text")
    private String reponseBrute;

    @Column(nullable = false)
    private long latenceMs;

    @Column(nullable = false)
    private boolean succes;

    @Column(length = 500)
    private String erreur;

    @Column(nullable = false)
    private Instant dateCreation;

    public JournalExtraction() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public String getReponseBrute() {
        return reponseBrute;
    }

    public void setReponseBrute(String reponseBrute) {
        this.reponseBrute = reponseBrute;
    }

    public long getLatenceMs() {
        return latenceMs;
    }

    public void setLatenceMs(long latenceMs) {
        this.latenceMs = latenceMs;
    }

    public boolean getSucces() {
        return succes;
    }

    public void setSucces(boolean succes) {
        this.succes = succes;
    }

    public String getErreur() {
        return erreur;
    }

    public void setErreur(String erreur) {
        this.erreur = erreur;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }
}

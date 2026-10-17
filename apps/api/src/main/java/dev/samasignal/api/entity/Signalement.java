package dev.samasignal.api.entity;

import dev.samasignal.api.enumeration.Categorie;
import dev.samasignal.api.enumeration.NiveauUrgence;
import dev.samasignal.api.enumeration.StatutSignalement;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "signalements")
public class Signalement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Version private long version;

    @Column(nullable = false, length = 5000)
    private String texteBrut;

    @Column(length = 500)
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Categorie categorie;

    @Column(nullable = false, length = 1000)
    private String resume;

    @Column(length = 500)
    private String localisationDeclaree;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NiveauUrgence niveauUrgence;

    @ElementCollection
    @CollectionTable(name = "elements_visuels", joinColumns = @JoinColumn(name = "signalement_id"))
    @OrderColumn(name = "position")
    @Column(name = "element", length = 1000)
    private List<String> elementsVisuelsDetectes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "champs_manquants", joinColumns = @JoinColumn(name = "signalement_id"))
    @OrderColumn(name = "position")
    @Column(name = "champ", length = 100)
    private List<String> champsManquants = new ArrayList<>();

    @Column(nullable = false, precision = 6, scale = 5)
    private BigDecimal scoreConfiance;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutSignalement statut;

    @Column(nullable = false, updatable = false)
    private Instant dateCreation;

    public Signalement() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getTexteBrut() {
        return texteBrut;
    }

    public void setTexteBrut(String texteBrut) {
        this.texteBrut = texteBrut;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Categorie getCategorie() {
        return categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public String getResume() {
        return resume;
    }

    public void setResume(String resume) {
        this.resume = resume;
    }

    public String getLocalisationDeclaree() {
        return localisationDeclaree;
    }

    public void setLocalisationDeclaree(String localisationDeclaree) {
        this.localisationDeclaree = localisationDeclaree;
    }

    public NiveauUrgence getNiveauUrgence() {
        return niveauUrgence;
    }

    public void setNiveauUrgence(NiveauUrgence niveauUrgence) {
        this.niveauUrgence = niveauUrgence;
    }

    public List<String> getElementsVisuelsDetectes() {
        return elementsVisuelsDetectes;
    }

    public void setElementsVisuelsDetectes(List<String> elementsVisuelsDetectes) {
        this.elementsVisuelsDetectes = elementsVisuelsDetectes;
    }

    public List<String> getChampsManquants() {
        return champsManquants;
    }

    public void setChampsManquants(List<String> champsManquants) {
        this.champsManquants = champsManquants;
    }

    public BigDecimal getScoreConfiance() {
        return scoreConfiance;
    }

    public void setScoreConfiance(BigDecimal scoreConfiance) {
        this.scoreConfiance = scoreConfiance;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public StatutSignalement getStatut() {
        return statut;
    }

    public void setStatut(StatutSignalement statut) {
        this.statut = statut;
    }

    public Instant getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(Instant dateCreation) {
        this.dateCreation = dateCreation;
    }

    @PrePersist
    void initialiser() {
        if (dateCreation == null) dateCreation = Instant.now();
    }
}

package dev.samasignal.api.dto.reponse;

import dev.samasignal.api.enumeration.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Fiche du signalement envoyée à l’application, sans exposer l’entité JPA. */
public record SignalementDto(
        UUID id,
        String texteBrut,
        String photoUrl,
        Categorie categorie,
        String resume,
        String localisationDeclaree,
        NiveauUrgence niveauUrgence,
        List<String> elementsVisuelsDetectes,
        List<String> champsManquants,
        BigDecimal scoreConfiance,
        BigDecimal latitude,
        BigDecimal longitude,
        StatutSignalement statut,
        Instant dateCreation) {}

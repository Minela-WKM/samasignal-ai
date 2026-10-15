package dev.samasignal.api.contract;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Flat HTTP response. Do not expose a JPA entity as the API contract. */
public record ReportResponse(
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
        ReportStatus statut,
        Instant createdAt
) {}

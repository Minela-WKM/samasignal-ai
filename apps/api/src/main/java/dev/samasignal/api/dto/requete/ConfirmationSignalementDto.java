package dev.samasignal.api.dto.requete;

import dev.samasignal.api.enumeration.*;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/** Données corrigées par le citoyen avant la confirmation du signalement. */
public record ConfirmationSignalementDto(
        @NotNull(message = "Ce champ est obligatoire.") Categorie categorie,
        @NotBlank(message = "Ce champ ne doit pas être vide.")
                @Size(max = 1000, message = "La taille maximale autorisée est de {max}.")
                String resume,
        @NotBlank(message = "Ce champ ne doit pas être vide.")
                @Size(max = 500, message = "La taille maximale autorisée est de {max}.")
                String localisationDeclaree,
        @NotNull(message = "Ce champ est obligatoire.") NiveauUrgence niveauUrgence,
        @NotNull(message = "Ce champ est obligatoire.")
                @DecimalMin(value = "-90", message = "La valeur minimale autorisée est {value}.")
                @DecimalMax(value = "90", message = "La valeur maximale autorisée est {value}.")
                BigDecimal latitude,
        @NotNull(message = "Ce champ est obligatoire.")
                @DecimalMin(value = "-180", message = "La valeur minimale autorisée est {value}.")
                @DecimalMax(value = "180", message = "La valeur maximale autorisée est {value}.")
                BigDecimal longitude) {}

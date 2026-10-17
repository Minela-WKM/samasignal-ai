package dev.samasignal.api.dto.ia;

import dev.samasignal.api.enumeration.*;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

/** Proposition structurée de l’IA, validée avant son utilisation par le service. */
public record ExtractionIaDto(
        @NotNull(message = "Ce champ est obligatoire.") Categorie categorie,
        @NotBlank(message = "Ce champ ne doit pas être vide.")
                @Size(max = 1000, message = "La taille maximale autorisée est de {max}.")
                String resume,
        @Size(max = 500, message = "La taille maximale autorisée est de {max}.")
                String localisationDeclaree,
        @NotNull(message = "Ce champ est obligatoire.") NiveauUrgence niveauUrgence,
        @NotNull(message = "Ce champ est obligatoire.")
                @Size(max = 100, message = "La taille maximale autorisée est de {max}.")
                List<
                                @NotBlank(message = "Ce champ ne doit pas être vide.")
                                @Size(
                                        max = 1000,
                                        message = "La taille maximale autorisée est de {max}.")
                                String>
                        elementsVisuelsDetectes,
        @NotNull(message = "Ce champ est obligatoire.")
                @Size(max = 100, message = "La taille maximale autorisée est de {max}.")
                List<
                                @NotBlank(message = "Ce champ ne doit pas être vide.")
                                @Size(
                                        max = 100,
                                        message = "La taille maximale autorisée est de {max}.")
                                String>
                        champsManquants,
        @NotNull(message = "Ce champ est obligatoire.")
                @DecimalMin(value = "0.0", message = "La valeur minimale autorisée est {value}.")
                @DecimalMax(value = "1.0", message = "La valeur maximale autorisée est {value}.")
                BigDecimal scoreConfiance) {}

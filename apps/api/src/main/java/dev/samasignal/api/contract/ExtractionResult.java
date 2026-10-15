package dev.samasignal.api.contract;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

/** AI proposal only. Coordinates and human corrections are separate. */
public record ExtractionResult(
        @NotNull Categorie categorie,
        @NotBlank @Size(max = 1000) String resume,
        @Size(max = 500) String localisationDeclaree,
        @NotNull NiveauUrgence niveauUrgence,
        @NotNull List<@NotBlank String> elementsVisuelsDetectes,
        @NotNull List<@NotBlank String> champsManquants,
        @NotNull @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal scoreConfiance
) {}

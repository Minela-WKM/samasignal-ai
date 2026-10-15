package dev.samasignal.api.contract;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** Full editable snapshot; not a patch and not an AI response. */
public record ConfirmReportRequest(
        @NotNull Categorie categorie,
        @NotBlank @Size(max = 1000) String resume,
        @NotBlank @Size(max = 500) String localisationDeclaree,
        @NotNull NiveauUrgence niveauUrgence,
        @NotNull @DecimalMin("-90") @DecimalMax("90") BigDecimal latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") BigDecimal longitude
) {}

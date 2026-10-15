package dev.samasignal.api.controller;

import dev.samasignal.api.dto.reponse.SignalementDto;
import dev.samasignal.api.dto.requete.ConfirmationSignalementDto;
import dev.samasignal.api.enumeration.Categorie;
import dev.samasignal.api.enumeration.NiveauUrgence;
import dev.samasignal.api.exception.ExceptionMetier;
import dev.samasignal.api.service.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;

@RestController
@RequestMapping("/api/v2/signalements")
@Tag(name = "Signalements", description = "Parcours citoyen et suivi municipal public")
public class SignalementControleur {
    private final SignalementService service;
    private final ExtractionIaService extraction;
    private final PhotoService photos;

    public SignalementControleur(
            SignalementService service, ExtractionIaService extraction, PhotoService photos) {
        this.service = service;
        this.extraction = extraction;
        this.photos = photos;
    }

    @PostMapping(value = "/analyser", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Analyser un signalement et créer sa fiche à vérifier")
    public ResponseEntity<SignalementDto> analyser(
            @RequestParam @NotBlank @Size(max = 5000) String texteBrut,
            @RequestPart(required = false) MultipartFile photo,
            @RequestParam(required = false) @DecimalMin("-90") @DecimalMax("90")
                    BigDecimal latitude,
            @RequestParam(required = false) @DecimalMin("-180") @DecimalMax("180")
                    BigDecimal longitude) {
        if ((latitude == null) != (longitude == null))
            throw new ExceptionMetier(
                    HttpStatus.BAD_REQUEST,
                    "COORDONNEES_INCOMPLETES",
                    "Fournissez la latitude et la longitude ensemble.");
        var image = photos.valider(photo);
        var resultat =
                extraction.extraire(
                        texteBrut.strip(),
                        image == null ? null : image.contenu(),
                        image == null ? null : image.type());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.creer(texteBrut, image, latitude, longitude, resultat));
    }

    @PostMapping("/{id}/confirmer")
    @Operation(summary = "Confirmer les champs corrigés par le citoyen")
    public SignalementDto confirmer(
            @PathVariable UUID id, @RequestBody @Valid ConfirmationSignalementDto requete) {
        return service.confirmer(id, requete);
    }

    @GetMapping
    @Operation(summary = "Lister les signalements confirmés ou traités")
    public List<SignalementDto> lister(
            @RequestParam(required = false) NiveauUrgence urgence,
            @RequestParam(required = false) Categorie categorie) {
        return service.lister(urgence, categorie);
    }

    @PostMapping("/{id}/traiter")
    @Operation(summary = "Marquer un signalement confirmé comme traité")
    public SignalementDto traiter(@PathVariable UUID id) {
        return service.traiter(id);
    }
}

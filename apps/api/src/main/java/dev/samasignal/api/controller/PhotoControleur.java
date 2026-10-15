package dev.samasignal.api.controller;

import dev.samasignal.api.exception.ExceptionMetier;
import dev.samasignal.api.repository.PhotoRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v2/photos")
@Tag(name = "Photos", description = "Consultation des photos des signalements")
public class PhotoControleur {
    private final PhotoRepository repository;

    public PhotoControleur(PhotoRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consulter une photo",
            description = "Renvoie une photo JPEG, PNG ou WebP à partir de son identifiant.")
    public ResponseEntity<byte[]> consulter(@PathVariable UUID id) {
        var p =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ExceptionMetier(
                                                HttpStatus.NOT_FOUND,
                                                "PHOTO_INTROUVABLE",
                                                "Cette photo n'existe pas."));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(p.getType()))
                .header("X-Content-Type-Options", "nosniff")
                .body(p.getContenu());
    }
}

package dev.samasignal.api.exception;

import dev.samasignal.api.dto.reponse.ErreurDto;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.*;

@RestControllerAdvice
public class GestionnaireExceptions {
    @ExceptionHandler(ExceptionMetier.class)
    ResponseEntity<ErreurDto> gererMetier(ExceptionMetier e) {
        return ResponseEntity.status(e.getStatut())
                .body(new ErreurDto(e.getCode(), e.getMessage(), Map.of()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErreurDto> gererValidation(MethodArgumentNotValidException e) {
        Map<String, String> champs = new LinkedHashMap<>();
        e.getBindingResult()
                .getFieldErrors()
                .forEach(f -> champs.put(f.getField(), f.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ErreurDto("VALIDATION_INVALIDE", "Vérifiez les champs saisis.", champs));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ErreurDto> gererTaille() {
        return ResponseEntity.status(413)
                .body(
                        new ErreurDto(
                                "PHOTO_TROP_VOLUMINEUSE",
                                "La photo ne doit pas dépasser 5 Mo.",
                                Map.of()));
    }

    @ExceptionHandler({
        org.springframework.http.converter.HttpMessageNotReadableException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class,
        jakarta.validation.ConstraintViolationException.class,
        org.springframework.web.method.annotation.HandlerMethodValidationException.class
    })
    ResponseEntity<ErreurDto> gererRequete(Exception e) {
        return ResponseEntity.badRequest()
                .body(
                        new ErreurDto(
                                "REQUETE_INVALIDE",
                                "La requête contient une valeur absente ou invalide.",
                                Map.of()));
    }

    @ExceptionHandler(org.springframework.dao.OptimisticLockingFailureException.class)
    ResponseEntity<ErreurDto> gererConflit() {
        return ResponseEntity.status(409)
                .body(
                        new ErreurDto(
                                "MODIFICATION_CONCURRENTE",
                                "Le signalement a été modifié. Rechargez la fiche.",
                                Map.of()));
    }
}

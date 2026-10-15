package dev.samasignal.api.dto.reponse;

import java.util.Map;

/** Message et détails de validation envoyés en cas d’erreur. */
public record ErreurDto(String code, String message, Map<String, String> erreursChamps) {}

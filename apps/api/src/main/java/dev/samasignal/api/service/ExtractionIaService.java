package dev.samasignal.api.service;

import dev.samasignal.api.dto.ia.ExtractionIaDto;

public interface ExtractionIaService {
    ExtractionIaDto extraire(String texte, byte[] photo, String typePhoto);
}

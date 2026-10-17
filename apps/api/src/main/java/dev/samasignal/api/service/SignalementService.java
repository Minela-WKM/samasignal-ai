package dev.samasignal.api.service;

import dev.samasignal.api.dto.ia.ExtractionIaDto;
import dev.samasignal.api.dto.reponse.SignalementDto;
import dev.samasignal.api.dto.requete.ConfirmationSignalementDto;
import dev.samasignal.api.enumeration.Categorie;
import dev.samasignal.api.enumeration.NiveauUrgence;
import dev.samasignal.api.service.PhotoService.PhotoValidee;

import java.math.BigDecimal;
import java.util.*;

public interface SignalementService {
    SignalementDto creer(
            String texte,
            PhotoValidee photo,
            BigDecimal latitude,
            BigDecimal longitude,
            ExtractionIaDto extraction);

    SignalementDto confirmer(UUID id, ConfirmationSignalementDto requete);

    List<SignalementDto> lister(NiveauUrgence urgence, Categorie categorie);

    SignalementDto traiter(UUID id);

    void supprimerBrouillon(UUID id);
}

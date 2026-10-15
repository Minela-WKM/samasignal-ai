package dev.samasignal.api.mapper;

import dev.samasignal.api.dto.ia.ExtractionIaDto;
import dev.samasignal.api.dto.reponse.SignalementDto;
import dev.samasignal.api.dto.requete.ConfirmationSignalementDto;
import dev.samasignal.api.entity.Signalement;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class SignalementMapper {

    public SignalementDto versDto(Signalement signalement) {
        return new SignalementDto(
                signalement.getId(),
                signalement.getTexteBrut(),
                signalement.getPhotoUrl(),
                signalement.getCategorie(),
                signalement.getResume(),
                signalement.getLocalisationDeclaree(),
                signalement.getNiveauUrgence(),
                List.copyOf(signalement.getElementsVisuelsDetectes()),
                List.copyOf(signalement.getChampsManquants()),
                signalement.getScoreConfiance(),
                signalement.getLatitude(),
                signalement.getLongitude(),
                signalement.getStatut(),
                signalement.getDateCreation());
    }

    /** Copie les champs extraits, sans décider du statut ni des champs manquants. */
    public Signalement versEntite(
            String texte, BigDecimal latitude, BigDecimal longitude, ExtractionIaDto extraction) {
        Signalement signalement = new Signalement();
        signalement.setTexteBrut(texte.strip());
        signalement.setCategorie(extraction.categorie());
        signalement.setResume(extraction.resume().strip());
        signalement.setLocalisationDeclaree(extraction.localisationDeclaree());
        signalement.setNiveauUrgence(extraction.niveauUrgence());
        signalement.setElementsVisuelsDetectes(
                new ArrayList<>(extraction.elementsVisuelsDetectes()));
        signalement.setScoreConfiance(extraction.scoreConfiance());
        signalement.setLatitude(latitude);
        signalement.setLongitude(longitude);
        return signalement;
    }

    /** Copie uniquement les champs que le citoyen est autorisé à corriger. */
    public void appliquerConfirmation(
            ConfirmationSignalementDto confirmation, Signalement signalement) {
        signalement.setCategorie(confirmation.categorie());
        signalement.setResume(confirmation.resume().strip());
        signalement.setLocalisationDeclaree(confirmation.localisationDeclaree().strip());
        signalement.setNiveauUrgence(confirmation.niveauUrgence());
        signalement.setLatitude(confirmation.latitude());
        signalement.setLongitude(confirmation.longitude());
    }
}

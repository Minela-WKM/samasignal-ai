package dev.samasignal.api.service.implementation;

import dev.samasignal.api.dto.ia.ExtractionIaDto;
import dev.samasignal.api.dto.reponse.SignalementDto;
import dev.samasignal.api.dto.requete.ConfirmationSignalementDto;
import dev.samasignal.api.entity.*;
import dev.samasignal.api.enumeration.Categorie;
import dev.samasignal.api.enumeration.NiveauUrgence;
import dev.samasignal.api.enumeration.StatutSignalement;
import dev.samasignal.api.exception.ExceptionMetier;
import dev.samasignal.api.mapper.SignalementMapper;
import dev.samasignal.api.repository.*;
import dev.samasignal.api.service.*;
import dev.samasignal.api.service.PhotoService.PhotoValidee;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class SignalementServiceImpl implements SignalementService {
    private final SignalementRepository repository;
    private final PhotoRepository photos;
    private final SignalementMapper mapper;

    public SignalementServiceImpl(
            SignalementRepository repository, PhotoRepository photos, SignalementMapper mapper) {
        this.repository = repository;
        this.photos = photos;
        this.mapper = mapper;
    }

    @Override
    @Transactional
    public SignalementDto creer(
            String texte,
            PhotoValidee photo,
            BigDecimal latitude,
            BigDecimal longitude,
            ExtractionIaDto extraction) {
        Signalement signalement = mapper.versEntite(texte, latitude, longitude, extraction);
        if (photo != null) {
            Photo photoEntite = new Photo();
            photoEntite.setContenu(photo.contenu());
            photoEntite.setType(photo.type());
            photos.save(photoEntite);
            signalement.setPhotoUrl("/api/v2/photos/" + photoEntite.getId());
        }
        signalement.setStatut(StatutSignalement.EN_ATTENTE);
        List<String> manquants = new ArrayList<>();
        if (extraction.localisationDeclaree() == null
                || extraction.localisationDeclaree().isBlank())
            manquants.add("localisationDeclaree");
        if (latitude == null) {
            manquants.add("latitude");
            manquants.add("longitude");
        }
        signalement.setChampsManquants(manquants);
        return mapper.versDto(repository.saveAndFlush(signalement));
    }

    @Override
    @Transactional
    public SignalementDto confirmer(UUID id, ConfirmationSignalementDto confirmation) {
        Signalement signalement = charger(id);
        if (signalement.getStatut() != StatutSignalement.EN_ATTENTE) {
            boolean identique =
                    signalement.getCategorie() == confirmation.categorie()
                            && signalement.getResume().equals(confirmation.resume().strip())
                            && Objects.equals(
                                    signalement.getLocalisationDeclaree(),
                                    confirmation.localisationDeclaree().strip())
                            && signalement.getNiveauUrgence() == confirmation.niveauUrgence()
                            && signalement.getLatitude().compareTo(confirmation.latitude()) == 0
                            && signalement.getLongitude().compareTo(confirmation.longitude()) == 0;
            if (!identique)
                throw new ExceptionMetier(
                        HttpStatus.CONFLICT,
                        "SIGNALEMENT_DEJA_CONFIRME",
                        "Un signalement confirmé ne peut plus être corrigé.");
            return mapper.versDto(signalement);
        }
        mapper.appliquerConfirmation(confirmation, signalement);
        signalement.getChampsManquants().clear();
        signalement.setStatut(StatutSignalement.CONFIRME);
        return mapper.versDto(signalement);
    }

    @Override
    public List<SignalementDto> lister(NiveauUrgence urgence, Categorie categorie) {
        return repository.rechercherPublics(urgence, categorie).stream()
                .map(mapper::versDto)
                .toList();
    }

    @Override
    @Transactional
    public SignalementDto traiter(UUID id) {
        Signalement signalement = charger(id);
        if (signalement.getStatut() == StatutSignalement.EN_ATTENTE)
            throw new ExceptionMetier(
                    HttpStatus.CONFLICT,
                    "SIGNALEMENT_NON_CONFIRME",
                    "Confirmez le signalement avant de le traiter.");
        signalement.setStatut(StatutSignalement.TRAITE);
        return mapper.versDto(signalement);
    }

    @Override
    @Transactional
    public void supprimerBrouillon(UUID id) {
        var brouillon = repository.rechercherPourModification(id);
        if (brouillon.isEmpty()) return;
        Signalement signalement = brouillon.get();
        if (signalement.getStatut() != StatutSignalement.EN_ATTENTE)
            throw new ExceptionMetier(
                    HttpStatus.CONFLICT,
                    "SIGNALEMENT_DEJA_CONFIRME",
                    "Un signalement confirmé ne peut plus être supprimé.");
        repository.delete(signalement);
        if (signalement.getPhotoUrl() != null) {
            String photoId = signalement.getPhotoUrl().substring("/api/v2/photos/".length());
            photos.deleteById(UUID.fromString(photoId));
        }
    }

    private Signalement charger(UUID id) {
        return repository
                .rechercherPourModification(id)
                .orElseThrow(
                        () ->
                                new ExceptionMetier(
                                        HttpStatus.NOT_FOUND,
                                        "SIGNALEMENT_INTROUVABLE",
                                        "Ce signalement n'existe pas."));
    }
}

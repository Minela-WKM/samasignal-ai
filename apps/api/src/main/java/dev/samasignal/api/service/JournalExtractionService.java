package dev.samasignal.api.service;

import dev.samasignal.api.entity.JournalExtraction;
import dev.samasignal.api.repository.JournalExtractionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

import java.time.Instant;

@Service
public class JournalExtractionService {
    private final JournalExtractionRepository repository;

    public JournalExtractionService(JournalExtractionRepository repository) {
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enregistrer(
            String modele, String reponse, long latence, boolean succes, String erreur) {
        JournalExtraction j = new JournalExtraction();
        j.setModele(modele);
        j.setReponseBrute(reponse);
        j.setLatenceMs(latence);
        j.setSucces(succes);
        j.setErreur(erreur);
        j.setDateCreation(Instant.now());
        repository.save(j);
    }
}

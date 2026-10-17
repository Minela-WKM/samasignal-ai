package dev.samasignal.api.repository;

import dev.samasignal.api.entity.Signalement;
import dev.samasignal.api.enumeration.Categorie;
import dev.samasignal.api.enumeration.NiveauUrgence;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface SignalementRepository extends JpaRepository<Signalement, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Signalement s where s.id = :id")
    Optional<Signalement> rechercherPourModification(@Param("id") UUID id);

    @Query(
            "select s from Signalement s where s.statut <>"
                + " dev.samasignal.api.enumeration.StatutSignalement.EN_ATTENTE and (:urgence is"
                + " null or s.niveauUrgence = :urgence) and (:categorie is null or s.categorie ="
                + " :categorie) order by s.dateCreation desc")
    List<Signalement> rechercherPublics(
            @Param("urgence") NiveauUrgence urgence, @Param("categorie") Categorie categorie);
}

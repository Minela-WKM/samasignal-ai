package dev.samasignal.api.repository;

import dev.samasignal.api.entity.JournalExtraction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JournalExtractionRepository extends JpaRepository<JournalExtraction, UUID> {}

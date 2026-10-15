
CREATE TABLE signalements (
 id UUID PRIMARY KEY, version BIGINT NOT NULL DEFAULT 0, texte_brut VARCHAR(5000) NOT NULL,
 photo_url VARCHAR(500), categorie VARCHAR(30) NOT NULL CHECK (categorie IN ('dechet','eau_usee','voirie','eclairage','autre')),
 resume VARCHAR(1000) NOT NULL, localisation_declaree VARCHAR(500), niveau_urgence VARCHAR(20) NOT NULL CHECK (niveau_urgence IN ('basse','moyenne','haute')),
 score_confiance NUMERIC(6,5) NOT NULL CHECK (score_confiance BETWEEN 0 AND 1),
 latitude NUMERIC(10,7) CHECK (latitude BETWEEN -90 AND 90), longitude NUMERIC(10,7) CHECK (longitude BETWEEN -180 AND 180),
 statut VARCHAR(20) NOT NULL CHECK (statut IN ('EN_ATTENTE','CONFIRME','TRAITE')), date_creation TIMESTAMP WITH TIME ZONE NOT NULL,
 CHECK ((latitude IS NULL) = (longitude IS NULL)),
 CHECK (statut = 'EN_ATTENTE' OR (latitude IS NOT NULL AND localisation_declaree IS NOT NULL AND LENGTH(TRIM(localisation_declaree)) > 0))
);
CREATE INDEX idx_signalements_suivi ON signalements(statut, niveau_urgence, categorie, date_creation);
CREATE TABLE elements_visuels (signalement_id UUID NOT NULL REFERENCES signalements(id) ON DELETE CASCADE, position INTEGER NOT NULL, element VARCHAR(1000), PRIMARY KEY(signalement_id, position));
CREATE TABLE champs_manquants (signalement_id UUID NOT NULL REFERENCES signalements(id) ON DELETE CASCADE, position INTEGER NOT NULL, champ VARCHAR(100), PRIMARY KEY(signalement_id, position));
CREATE TABLE journaux_extraction (id UUID PRIMARY KEY, modele VARCHAR(100) NOT NULL, reponse_brute TEXT, latence_ms BIGINT NOT NULL, succes BOOLEAN NOT NULL, erreur VARCHAR(500), date_creation TIMESTAMP WITH TIME ZONE NOT NULL);
CREATE TABLE photos (id UUID PRIMARY KEY, contenu BYTEA NOT NULL, type VARCHAR(30) NOT NULL);

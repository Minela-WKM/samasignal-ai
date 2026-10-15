-- Données entièrement synthétiques. Chargement explicite après migrations Flyway.
-- psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f docs/donnees-demo.sql
INSERT INTO signalements (id, version, texte_brut, categorie, resume, localisation_declaree, niveau_urgence, score_confiance, latitude, longitude, statut, date_creation) VALUES
('00000000-0000-4000-8000-000000000001', 0, 'Exemple synthétique 1', 'dechet', 'Incident de démonstration 1', 'Dakar, secteur de démonstration 1', 'basse', 0.8, 14.701, -17.451, 'CONFIRME', CURRENT_TIMESTAMP),
('00000000-0000-4000-8000-000000000002', 0, 'Exemple synthétique 2', 'eau_usee', 'Incident de démonstration 2', 'Dakar, secteur de démonstration 2', 'moyenne', 0.8, 14.702, -17.452, 'CONFIRME', CURRENT_TIMESTAMP),
('00000000-0000-4000-8000-000000000003', 0, 'Exemple synthétique 3', 'voirie', 'Incident de démonstration 3', 'Dakar, secteur de démonstration 3', 'haute', 0.8, 14.703, -17.453, 'CONFIRME', CURRENT_TIMESTAMP),
('00000000-0000-4000-8000-000000000004', 0, 'Exemple synthétique 4', 'eclairage', 'Incident de démonstration 4', 'Dakar, secteur de démonstration 4', 'basse', 0.8, 14.704, -17.454, 'CONFIRME', CURRENT_TIMESTAMP),
('00000000-0000-4000-8000-000000000005', 0, 'Exemple synthétique 5', 'autre', 'Incident de démonstration 5', 'Dakar, secteur de démonstration 5', 'moyenne', 0.8, 14.705, -17.455, 'CONFIRME', CURRENT_TIMESTAMP),
('00000000-0000-4000-8000-000000000006', 0, 'Exemple synthétique 6', 'dechet', 'Incident de démonstration 6', 'Dakar, secteur de démonstration 6', 'haute', 0.8, 14.706, -17.456, 'CONFIRME', CURRENT_TIMESTAMP),
('00000000-0000-4000-8000-000000000007', 0, 'Exemple synthétique 7', 'eau_usee', 'Incident de démonstration 7', 'Dakar, secteur de démonstration 7', 'basse', 0.8, 14.707, -17.457, 'CONFIRME', CURRENT_TIMESTAMP),
('00000000-0000-4000-8000-000000000008', 0, 'Exemple synthétique 8', 'voirie', 'Incident de démonstration 8', 'Dakar, secteur de démonstration 8', 'moyenne', 0.8, 14.708, -17.458, 'CONFIRME', CURRENT_TIMESTAMP),
('00000000-0000-4000-8000-000000000009', 0, 'Exemple synthétique 9', 'eclairage', 'Incident de démonstration 9', 'Dakar, secteur de démonstration 9', 'haute', 0.8, 14.709, -17.459, 'CONFIRME', CURRENT_TIMESTAMP),
('00000000-0000-4000-8000-000000000010', 0, 'Exemple synthétique 10', 'autre', 'Incident de démonstration 10', 'Dakar, secteur de démonstration 10', 'basse', 0.8, 14.710, -17.460, 'CONFIRME', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

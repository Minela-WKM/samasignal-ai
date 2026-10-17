# Architecture SamaSignal

Le navigateur appelle Spring Boot via le proxy Vite en développement et nginx dans Docker.
L'API écoute sur 8081 et expose ses routes métier sous `/api/v2`.

## Couches backend

`controller` → `service` → `repository` → PostgreSQL.
Les entités sont dans `entity`, les DTO dans `dto` et les conversions dans `mapper`.
Les erreurs HTTP passent par `GestionnaireExceptions`. Swagger décrit les routes exécutables.

1. `POST /api/v2/signalements/analyser` valide texte, coordonnées et photo avant l'appel IA.
2. `ExtractionIaService` appelle Gemma avec texte et image éventuelle, puis valide `ExtractionIaDto`.
3. `JournalExtractionService` conserve chaque tentative dans une transaction indépendante.
4. Le service persiste le brouillon et sa photo atomiquement, avec statut `EN_ATTENTE`.
5. `POST /api/v2/signalements/{id}/confirmer` applique les corrections validées et passe à `CONFIRME`.
6. `GET /api/v2/signalements` retourne uniquement les fiches confirmées et traitées, filtrées par urgence/catégorie.
7. `POST /api/v2/signalements/{id}/traiter` clôt le suivi ; une fiche en attente est refusée.

## Persistance

PostgreSQL est utilisé en local et dans Docker. Flyway crée le schéma et Hibernate le valide.
Les photos sont stockées en `bytea` dans la même base ; le volume PostgreSQL assure leur persistance.
Les collections de champs manquants et d'éléments visuels utilisent des tables séparées ordonnées.
La confirmation utilise un verrou de ligne et reste idempotente pour des valeurs identiques.
H2 en mode PostgreSQL est réservé aux tests ; une recette sur PostgreSQL reste nécessaire.

## Fournisseur IA

Variables serveur : `GEMINI_API_KEY`, `GEMMA_MODEL`, `AI_MODE=hosted`.
Le mode `disabled` retourne une indisponibilité explicite, sans extraction simulée.
L'adaptateur utilise le protocole REST officiel Google `generateContent`, avec délai maximal de 30 secondes.
Il valide le JSON contre les contraintes du DTO et journalise les erreurs sans créer de brouillon.
La localisation et les coordonnées ne sont jamais inventées ; les champs manquants métier sont recalculés.
La validation réelle du modèle avec clé fournisseur et photo doit être effectuée avant la démonstration.

## Périmètre

L'accès est public, conformément au cahier des charges : aucune authentification ni rôle municipal.
Les journaux IA ne sont pas exposés par l'API. Le frontend métier et SamaA11y sont intégrés dans cette branche.

## Intégration et Render

Le frontend utilise les adresses `/api/v2/signalements`, `dateCreation` et `erreursChamps` du backend.
La gestion des délais et des annulations du frontend est conservée.
Nginx génère sa configuration au démarrage à partir de `PORT`, `API_SCHEME` et `API_HOST`.
Compose conserve `http://api:8081` ; Render utilise l’hôte HTTPS public de l’API.
Le profil Spring `render` reçoit les propriétés de la base gérée, sans URL ou secret versionné.

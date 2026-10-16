# Architecture du socle et cible MVP

## Socle livré

Le navigateur charge Vue 3/Vite/Tailwind. En développement Vite relaie les appels vers
Spring Boot sur 8081 ; dans Docker, nginx sert le build et fait le même proxy. Le backend
embarque Web, Validation, JPA, Actuator et springdoc-openapi. Il utilise H2 en mémoire par
défaut et PostgreSQL avec le profil `docker`.

Le frontend propose un parcours citoyen complet et une supervision conservés avec `KeepAlive`.
Il appelle les routes du contrat, mais le backend de cette branche reste un socle sans routes
métier. Swagger décrit le backend réel ; `openapi.yaml` reste le contrat cible inchangé.

Le choix Spring Boot est fondé sur la vélocité de Dev B, responsable du backend.
JDK 21, Spring Boot 3.5 et Maven Wrapper sont la base commune ; le frontend utilise Node 24.

## Flux MVP frontend et intégration backend

1. Le citoyen saisit du texte et éventuellement une photo ; il peut déjà choisir un point.
2. `/analyze` valide les entrées, appelle Gemma et valide sa réponse, puis crée `EN_ATTENTE`.
3. Le citoyen corrige la fiche, complète le lieu et sélectionne les coordonnées.
4. `/{id}/confirm` vérifie les champs et passe la fiche à `CONFIRME`.
5. `/reports` expose seulement les fiches `CONFIRME`/`TRAITE`, filtrées et ordonnées.

La sortie IA reste un `ExtractionResult` à sept champs. Les réponses HTTP sont des
`ReportResponse` plats ; les coordonnées, identifiant, statut et dates appartiennent au système.
Le client frontend centralise ses appels dans `src/lib/api.js` : délais 35 s (analyse) et
15 s (liste/confirmation), annulation et validation des réponses. Aucun POST n'est réessayé
automatiquement. Le brouillon reste en mémoire uniquement, sans localStorage.

## Repli dashboard adopté le 16 octobre

Avant tout chargement réel réussi, un échec réseau, timeout, 404 ou 5xx de la liste affiche
cinq fixtures synthétiques avec un bandeau permanent et un bouton Réessayer. Une liste vide,
une réponse invalide ou 401/403 ne déclenche pas ce repli. Après succès réel, les données réelles
sont conservées lors d'une panne et signalées comme potentiellement obsolètes. Une confirmation
citoyenne récente empêche également le repli fictif. Les sources ne sont jamais mélangées.
Analyse et confirmation restent réelles.

Les marqueurs partagent les filtres du tableau ; un incident sans coordonnées reste consultable
sans marqueur. Les popups utilisent des nœuds texte sûrs. Un fond OSM indisponible n'empêche pas
la saisie des coordonnées au clavier ni la consultation des informations textuelles.

## Persistance à ajouter par Dev B

- `IncidentReport` : champs du contrat, texte brut, référence photo, coordonnées et statut.
- `AIExtractionLog` : modèle, réponse brute, latence et succès/erreur, avec lien éventuel à l'incident.
- Migrations versionnées avant les premières entités ; ne pas activer `ddl-auto=update` en Docker.
- Photos contrôlées (JPEG/PNG/WebP, 5 Mo max), nom généré, volume `/app/uploads`.
  Ne pas accepter de téléchargement distant arbitraire à partir d'une URL fournie par le client.
- PostgreSQL et photos persistent dans des volumes distincts. H2 est volontairement éphémère.

Les logs IA ne sont pas publics et ne doivent contenir ni clé ni données de démonstration privées.
Les dix fixtures de J2 sont synthétiques et chargées explicitement, jamais silencieusement.

## Intégration IA à valider

`GEMINI_API_KEY`, `GEMMA_MODEL` et `AI_MODE` sont réservés au futur adaptateur.
Le socle accepte ces variables dans Compose mais ne les consomme pas encore.
Le mode courant est `disabled`. Dev B devra implémenter les modes explicites `live` et `demo`.
Une erreur en mode réel ne déclenche jamais une simulation cachée.

Tester Gemma hébergé avec texte + image dès J1. Utiliser un schéma de sortie explicite si
supporté, puis une validation serveur systématique. Si le fournisseur refuse le schéma,
demander un JSON strict dans le prompt et valider le même DTO ; ne pas accepter un JSON invalide.
Timeout cible : 30 secondes. Renvoyer une erreur exploitable et conserver la saisie côté UI.
L'IA ne choisit pas les coordonnées, ne valide pas elle-même une fiche et ne décide pas des règles serveur.

## Limites du MVP

Pas de login, d'autorisation mairie, de géocodage automatique ni d'endpoint de passage à `TRAITE`.
La carte est centrée sur Dakar ; les coordonnées restent une saisie humaine vérifiée.
SamaA11y est indépendant du pipeline IA et n'ajoute aucun écran.

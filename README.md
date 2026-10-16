# SamaSignal AI

**Un signalement citoyen, une fiche claire, une action à suivre.**

Projet open source du hackathon Galsen DEV 2026, track Civic Tech & services publics.
À Dakar, SamaSignal AI vise à transformer un texte et une photo en incident structuré,
corrigé et confirmé par le citoyen, puis visible dans un dashboard municipal public.

## État du projet

Ce dépôt contient le **frontend complet et le socle backend**. Les routes métier et le
pipeline IA restent à livrer par Dev B ; aucun succès d'analyse/confirmation n'est simulé.

| Disponible | À développer sur les branches dédiées |
| --- | --- |
| Vue 3, Tailwind, Leaflet : parcours citoyen complet et supervision | Recette des routes réelles avec Dev B |
| Spring Boot, JPA, H2/PostgreSQL, healthcheck et Swagger | Entités, migrations, routes de signalement et pipeline IA |
| Contrat OpenAPI et DTO Java | Intégration Gemma 4 et validation réelle texte + image |
| Docker Compose, tests E2E, SamaA11y et workflow CI | Datasets backend et vidéo de démonstration |

Les routes `/api/reports` sont **contractuelles et non implémentées** : elles ne retournent
pas de faux succès. Aucun appel IA n'est effectué par le socle. Le healthcheck mesure
l'application et sa base, pas l'accès à Gemma. Le dashboard peut afficher cinq exemples
synthétiques clairement signalés lors d'une indisponibilité initiale. Ces exemples ne sont
jamais envoyés aux routes d'analyse/confirmation. Une réponse vide ne déclenche pas la démo.

## Démarrage avec Docker

Prérequis : Git, Docker Engine ou Docker Desktop démarré avec Compose v2, accès Internet
pour télécharger les images et dépendances. Depuis la racine :

```bash
docker compose up --build --wait
```

Le premier build télécharge Maven et les dépendances Java/Node. Les valeurs de base locales
permettent de démarrer sans clé IA et sans copier de fichier de configuration.

- Application : http://localhost:8080
- Healthcheck API : http://localhost:8081/actuator/health
- Swagger (routes réellement implémentées) : http://localhost:8081/swagger-ui/index.html
- OpenAPI généré : http://localhost:8081/v3/api-docs
- Contrat métier cible : [docs/openapi.yaml](docs/openapi.yaml)

Swagger ne contient pas encore les routes métier : leur contrat cible est versionné séparément.
Pour modifier les ports ou préparer la clé IA, copier `.env.example` vers `.env` à la racine.
Ne jamais placer cette clé dans une variable `VITE_*` ou dans le navigateur.

```bash
docker compose logs -f api
docker compose down
```

Les volumes PostgreSQL et photos sont conservés par `down`. `down --volumes` efface ces
données locales. Les ports sont liés à localhost ; ce Compose est destiné au développement,
pas à une exposition Internet avec les identifiants de démonstration.

## Développement sans Docker

Prérequis : **Node.js 24**, **JDK 21** avec `JAVA_HOME` défini. Maven est téléchargé par le Wrapper.

Backend, dans un terminal :

```bash
cd apps/api
./mvnw spring-boot:run
```

Sur PowerShell, remplacer `./mvnw` par `.\mvnw.cmd`. Le profil `local` utilise H2 en mémoire
sur le port 8081. Les données H2 sont perdues au redémarrage. Le backend ne lit pas
automatiquement le fichier `.env` de Compose : exporter les variables nécessaires dans le terminal.

Frontend, dans un autre terminal :

```bash
cd apps/web
npm ci
npm run dev
```

Ouvrir l'URL affichée par Vite (par défaut http://localhost:5173). Son proxy relaie les appels
API et Swagger vers `127.0.0.1:8081`, sans configuration CORS supplémentaire.

## Vérifications

```bash
# Depuis apps/api (mvnw.cmd sur Windows)
./mvnw -B -ntp verify

# Depuis apps/web
npm ci
npm test
npm run build

# Depuis la racine, Docker démarré
docker compose config --quiet
docker compose up --build --wait
```

GitHub Actions exécute sur chaque push de branche et chaque PR vers `main` : tests/build
frontend, tests/build backend et démarrage Compose avec PostgreSQL, healthchecks et Swagger.
Les tests E2E et SamaA11y utilisent une API et des tuiles simulées, sans secret IA.
La CI publie les rapports JSON/Markdown et captures SamaA11y en artefact.

## Organisation et responsabilités

```text
apps/web/        Vue 3 + Vite + Tailwind — Dev A (Minela)
apps/api/        Spring Boot 3.5 + Java 21 — Dev B
tools/sama-a11y/ CLI Playwright + axe-core — Dev A
docs/           Architecture, contrat et planning communs
tests/          Scénarios de recette transverses
```

- Dev A : branche `feature/frontend-vue`, frontend, SamaA11y, Docker/CI et vidéo.
- Dev B : branche `feature/backend-spring`, API, IA, règles métier, tests et données de démo.
- PR courtes, review croisée, fusion après CI verte et première intégration au plus tard le 16 octobre.
- Premier commit local : **15 octobre 2026**. L'historique réel ne doit pas être antidaté.

Lire [CONTRIBUTING.md](CONTRIBUTING.md), [l'architecture](docs/architecture.md),
[le contrat](docs/openapi.yaml) et [le planning](docs/implementation-plan.md) avant de diverger.

## Périmètre et modèle IA

MVP prévu : saisie texte/photo → extraction IA → correction humaine → confirmation → liste
filtrable et carte. La localisation est choisie par le citoyen ; l'IA n'invente pas les coordonnées.

Modèle prévu : **Gemma 4**, modèle open-weight sous Apache 2.0, appelé via API hébergée
avec le SDK Google GenAI Java. Identifiant initial prévu : `gemma-4-26b-a4b-it`.
L'accès réel avec la clé, les entrées image et le JSON structuré restent à valider par Dev B.
Références : [Gemma 4](https://ai.google.dev/gemma/docs/core),
[API hébergée](https://ai.google.dev/gemma/docs/core/gemma_on_gemini_api),
[SDK Java](https://github.com/googleapis/java-genai).

Hors MVP : authentification/JWT, rôles, tournées optimisées, RAG, prédiction et inférence locale.
Les vues citoyen/agent seront publiques : ce prototype ne convient pas à des signalements
sensibles. Les fixtures doivent être synthétiques et les photos réutilisables avec autorisation.

## Accessibilité et livraison

SamaA11y est une CLI indépendante, sans écran ni base de données, générant JSON et Markdown.
Voir [son guide](tools/sama-a11y/README.md) et [le guide frontend](apps/web/README.md).
Cible automatisée : **WCAG 2.1 AA** via axe-core. Ces tests ne constituent ni une certification
de conformité ni un audit **RGAA**, qui demande aussi des contrôles manuels.

Livrables prévus pour le 19 octobre 2026 : dépôt public, README/API documentés, démonstration
Docker et vidéo de 3 minutes maximum. Code freeze prévu à 12 h (UTC / Atlantic-Reykjavik).

## Licence

Code du projet sous [MIT](LICENSE). Les modèles, dépendances et données conservent leurs
licences respectives. Aucun code SaniData n'est repris comme socle.

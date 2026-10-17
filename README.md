# SamaSignal AI

**Un signalement citoyen, une fiche claire, une action à suivre.**

Projet open source du hackathon Galsen DEV 2026, track Civic Tech & services publics.
À Dakar, SamaSignal AI vise à transformer un texte et une photo en incident structuré,
corrigé et confirmé par le citoyen, puis visible dans un dashboard municipal public.

## État du projet

Le backend fournit une API MVC en français sous `/api/v2/signalements` : analyse IA,
confirmation citoyenne, liste filtrée et passage au statut traité. Il utilise PostgreSQL,
des migrations Flyway et des DTO distincts des entités JPA.
La branche d’intégration inclut le formulaire citoyen, la correction, le tableau de bord,
la carte Leaflet et la CLI SamaA11y issus du frontend. La recette avec Gemma réel et la mise en ligne restent à effectuer.

Le healthcheck mesure l'application et sa base, pas l'accès à Gemma.
Voir [le guide de l'API](apps/api/README.md) pour l'architecture et les exemples de requêtes.

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

Swagger décrit les routes métier disponibles.
Pour modifier les ports ou préparer la clé IA, copier `.env.example` vers `.env` à la racine.
Ne jamais placer cette clé dans une variable `VITE_*` ou dans le navigateur.

```bash
docker compose logs -f api
docker compose down
```

Les photos sont stockées dans PostgreSQL ; le volume de base est conservé par `down`. `down --volumes` efface ces
données locales. Les ports sont liés à localhost ; ce Compose est destiné au développement,
pas à une exposition Internet avec les identifiants de démonstration.

## Développement sans Docker

Prérequis : **Node.js 24**, **JDK 21** avec `JAVA_HOME` défini. Maven est téléchargé par le Wrapper.

Backend, dans un terminal :

```bash
cd apps/api
./mvnw spring-boot:run
```

Sur PowerShell, remplacer `./mvnw` par `.\mvnw.cmd`. Le profil `local` utilise PostgreSQL
et nécessite une base existante et `POSTGRES_PASSWORD` ; voir le guide API. Le serveur écoute sur 8081. Le backend ne lit pas
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
La CI du socle n'utilise aucun secret IA. Le contrôle SamaA11y sera ajouté dès que sa CLI existe.

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
avec l’API REST Google `generateContent`. Identifiant configuré par défaut : `gemma-4-26b-a4b-it`.
L'accès réel avec la clé, les entrées image et le JSON structuré restent à valider par Dev B.
Références : [Gemma 4](https://ai.google.dev/gemma/docs/core),
[API hébergée](https://ai.google.dev/gemma/docs/core/gemma_on_gemini_api),
[SDK Java](https://github.com/googleapis/java-genai).

Hors MVP : authentification/JWT, rôles, tournées optimisées, RAG, prédiction et inférence locale.
Les vues citoyen/agent seront publiques : ce prototype ne convient pas à des signalements
sensibles. Les fixtures doivent être synthétiques et les photos réutilisables avec autorisation.

## Accessibilité et livraison

SamaA11y est une CLI indépendante, sans écran ni base de données, générant JSON et Markdown.
Cible automatisée : **WCAG 2.1 AA** via axe-core. Ces tests ne constituent ni une certification
de conformité ni un audit **RGAA**, qui demande aussi des contrôles manuels.

Livrables prévus pour le 19 octobre 2026 : dépôt public, README/API documentés, démonstration
Docker et vidéo de 3 minutes maximum. Code freeze prévu à 12 h (UTC / Atlantic-Reykjavik).

## Licence

Code du projet sous [MIT](LICENSE). Les modèles, dépendances et données conservent leurs
licences respectives. Aucun code SaniData n'est repris comme socle.

## Préparation Render

Le fichier `render.yaml` prépare deux services Docker et PostgreSQL, avec déploiement depuis
`main` après réussite de la CI. Il ne déclenche pas de création de services à lui seul.
Voir [le guide Render](docs/deploiement-render.md) pour la fusion, les secrets et la recette.

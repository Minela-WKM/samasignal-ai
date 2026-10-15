# API SamaSignal — Spring MVC et PostgreSQL

Java 21, Spring Boot 3.5, Spring Data JPA, PostgreSQL et Flyway.

## Organisation du code

Le projet reste en Java Spring Boot, avec des dossiers séparés par responsabilité.
Cette organisation reprend la séparation contrôleurs, entités, services et repositories que tu souhaites.

```text
src/main/java/dev/samasignal/api/
├── controller/                  Reçoit les appels de l'application
│   ├── SignalementControleur.java
│   └── PhotoControleur.java
├── dto/                         Données reçues ou envoyées par l'API
│   ├── requete/
│   │   └── ConfirmationSignalementDto.java
│   ├── reponse/
│   │   ├── SignalementDto.java
│   │   └── ErreurDto.java
│   └── ia/
│       └── ExtractionIaDto.java
├── entity/                      Objets enregistrés dans PostgreSQL
│   ├── Signalement.java
│   ├── Photo.java
│   └── JournalExtraction.java
├── enumeration/                 Choix possibles : catégorie, urgence, statut
├── mapper/
│   └── SignalementMapper.java   Convertit les entités et les DTO
├── repository/                  Lit et enregistre les données
├── service/                     Effectue les traitements et vérifications métier
│   └── implementation/         Implémentations des services
├── exception/                   Prépare les messages d'erreur
└── config/                      Configuration de l'application
```

**Un DTO** est un objet qui transporte des données. Par exemple, `ConfirmationSignalementDto`
contient les corrections envoyées par le citoyen ; `SignalementDto` contient la fiche renvoyée
à l'application. `ExtractionIaDto` représente les données proposées par l'IA et `ErreurDto`
les informations d'une erreur. Les DTO sont des `record` Java : leurs données sont accessibles
par des méthodes comme `resume()` et ne sont pas modifiables après leur création.

**Une entité** est un objet enregistré dans la base de données. Les classes du dossier `entity`
possèdent des getters et setters explicites, par exemple `getResume()` et `setResume(...)`.
Elles ne sont pas renvoyées directement au navigateur.

**Un mapper** copie les données entre les objets. `SignalementMapper` propose :

- `versDto` : transforme une entité en fiche à envoyer à l'application.
- `versEntite` : prépare une entité à partir du texte, des coordonnées et de l'extraction IA.
- `appliquerConfirmation` : copie les corrections autorisées du DTO vers l'entité.

Le mapper est injecté dans le service par Spring. Le service décide du statut et des règles
métier ; le repository communique avec PostgreSQL ; le contrôleur reçoit l'appel et renvoie la réponse.

Le modèle IA propose les champs ; le citoyen fournit les coordonnées et confirme les valeurs finales.
Les brouillons `EN_ATTENTE` sont exclus du tableau de bord. La confirmation identique est idempotente ;
une correction après confirmation retourne 409. Une fiche confirmée peut passer à `TRAITE`.
Les modifications sont protégées par verrou transactionnel et version JPA.

## Démarrage local

Créer une base PostgreSQL nommée **sma signalement**. Comme ce nom contient un espace,
utiliser des guillemets doubles dans PostgreSQL :

```sql
CREATE DATABASE "sma signalement";
```

L'API est configurée pour PostgreSQL, mais la connexion à votre instance ne sera établie
qu'après le renseignement de vos identifiants et le démarrage de PostgreSQL.
Flyway crée les tables, pas la base elle-même.

Définir les variables dans le terminal (les identifiants sont à renseigner) :

```bash
export POSTGRES_HOST=localhost
export POSTGRES_PORT=5432
export POSTGRES_DB='sma signalement'
export POSTGRES_USER='votre-utilisateur'
export POSTGRES_PASSWORD='votre-mot-de-passe'
export AI_MODE=hosted
export GEMINI_API_KEY='votre-cle-serveur'
export GEMMA_MODEL=gemma-4-26b-a4b-it
./mvnw spring-boot:run
```

Le profil local utilise PostgreSQL. Flyway crée les tables ; Hibernate vérifie leur cohérence
avec les entités (`ddl-auto: validate`). Ne pas modifier une migration déjà exécutée : ajouter une nouvelle version.
Le profil `docker` utilise la base du Compose. H2 est réservé aux tests, en mode PostgreSQL.

Depuis la racine du dépôt : `docker compose up --build`. L'analyse est désactivée par défaut ;
configurer `AI_MODE=hosted` et la clé dans l'environnement ou le `.env` local non versionné.
Sans clé valide, l'API retourne 503, sans résultat simulé.

## Adresses de l’API

Une adresse d’API (aussi appelée « route » ou « endpoint ») indique où envoyer une demande.
Par exemple, `GET /api/v2/signalements` demande la liste des signalements.

| Méthode | Adresse | Rôle |
|---|---|---|
| POST | `/api/v2/signalements/analyser` | Multipart : `texteBrut`, `photo` facultative, `latitude` et `longitude` facultatives ensemble ; réponse 201 |
| POST | `/api/v2/signalements/{id}/confirmer` | JSON complet des champs corrigés ; réponse 200 |
| GET | `/api/v2/signalements?urgence=haute&categorie=dechet` | Fiches confirmées et traitées, filtres combinés |
| POST | `/api/v2/signalements/{id}/traiter` | Marquer la fiche comme traitée |
| GET | `/api/v2/photos/{id}` | Consulter une photo enregistrée |

```bash
curl -X POST http://localhost:8081/api/v2/signalements/analyser \
  -F 'texteBrut=Des déchets bloquent le trottoir près du marché de Grand Yoff'

curl -X POST http://localhost:8081/api/v2/signalements/UUID/confirmer \
  -H 'Content-Type: application/json' \
  -d '{"categorie":"dechet","resume":"Déchets sur le trottoir","localisationDeclaree":"Grand Yoff, Dakar","niveauUrgence":"haute","latitude":14.741,"longitude":-17.455}'
```

Swagger : http://localhost:8081/swagger-ui.html (redirige vers `/swagger-ui/index.html`).
La méthode `documentationSwagger()` de `config/OpenApiConfiguration.java` configure sa documentation.
Après démarrage de l'API, ouvrir Swagger, sélectionner une méthode puis **Try it out** et **Execute**
pour la tester. Les adresses des signalements et des photos y sont regroupées par contrôleur.
Documentation JSON générée : http://localhost:8081/v3/api-docs.
Les erreurs contiennent `code`, `message`, `erreursChamps`. Les dates utilisent `dateCreation`.
L'accès reste public conformément au MVP, y compris le changement de statut.

## IA et photos

L'adaptateur utilise l'API REST officielle Google `generateContent` (texte et image inline),
avec une limite de 30 secondes. Référence : https://ai.google.dev/api/generate-content.
Le modèle est configurable et doit être accessible avec la clé utilisée.
Chaque tentative conserve modèle, réponse brute, latence et résultat dans `journaux_extraction`.
Aucun signalement n'est créé si l'extraction échoue ou ne respecte pas le format attendu.
Les photos JPEG, PNG et WebP (5 Mo maximum) sont vérifiées par leur signature et type déclaré,
puis stockées dans PostgreSQL pour garantir une création atomique avec le signalement.
Les journaux contiennent potentiellement des données citoyennes : ne pas les exposer publiquement.

## Vérification

```bash
./mvnw -B -ntp verify
```

Les tests utilisent H2 et un adaptateur IA simulé : parcours complet, exclusion des brouillons,
filtres, confirmation idempotente, conflits, validation, fausse photo et échec IA sans création.
La version précédente a également été vérifiée sur PostgreSQL 17.
La réorganisation des classes est vérifiée par la même suite de 12 tests sur H2.
Pour la reproduire sur une base PostgreSQL **réservée aux tests** (les tests suppriment ses signalements) :

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/samasignal_test \
SPRING_DATASOURCE_USERNAME=samasignal \
SPRING_DATASOURCE_PASSWORD='mot-de-passe-test' \
./mvnw -B -ntp verify
```

Les appels réels à Gemma avec clé fournisseur restent à vérifier.
Dix exemples synthétiques sont disponibles dans `docs/donnees-demo.sql`, depuis la racine :
`psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f docs/donnees-demo.sql`.
Leur chargement est explicite et idempotent ; ils ne constituent pas une preuve d'appel IA.

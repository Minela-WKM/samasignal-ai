# Backend — Dev B

Branche à créer par Dev B : `feature/backend-spring`, depuis le `main` publié.
Java 21, Spring Boot 3.5, Maven Wrapper 3.9.11, JPA, Validation, Actuator et springdoc.

```bash
./mvnw -B -ntp verify
./mvnw spring-boot:run
```

PowerShell : `.\mvnw.cmd`. Définir `JAVA_HOME` vers un JDK 21. Les scripts Wrapper proviennent
de la distribution officielle Apache Maven Wrapper 3.3.4 (`only-script`, sans JAR versionné).
Le premier lancement nécessite Internet ; aucun Maven système n'est requis.

Profil par défaut : `local` (H2 en mémoire). Profil `docker` : PostgreSQL avec les variables
du Compose. JPA ne crée pas de tables dans ce socle. Ajouter des migrations avant les entités.

- Port : 8081 ; healthcheck : `/actuator/health`.
- Swagger : `/swagger-ui/index.html` ; OpenAPI généré : `/v3/api-docs`.
- DTO et enums partagés : package `dev.samasignal.api.contract`.
- Contrat cible : [OpenAPI](../../docs/openapi.yaml).

Les contrôleurs métier, entités et service Gemma ne sont pas encore implémentés. Ne pas
renvoyer de succès fictif ni laisser une `UnsupportedOperationException` sur une route exposée.
Dev B pilote l'adaptateur IA, les migrations, la persistance des photos, les logs et la validation.
La clé est une variable serveur uniquement. Valider l'appel texte+image et le JSON réel dès J1.

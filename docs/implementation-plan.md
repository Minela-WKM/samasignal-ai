# Plan d'exécution — 15 au 19 octobre 2026

Le socle est commun sur `main`, puis Dev A et Dev B partent du même commit.
Le socle initial ne livrait ni pipeline IA ni parcours métier. La branche frontend livre maintenant
le parcours, la supervision, les tests E2E et SamaA11y. L'intégration réelle dépend des routes de Dev B.
Premier commit local réel : 15 octobre.

| Jalon | Dev A — Minela | Dev B | Acceptation |
| --- | --- | --- | --- |
| J1 · 15 octobre | Socle, CI/Docker, branche frontend, formulaire accessible | Branche backend, test réel Gemma texte+photo, entités et `/analyze` | Builds verts, build Docker à froid et preuve IA réelle |
| J2 · 16 octobre | Intégration, complétion, carte, dashboard | `/confirm`, liste/filtres, migrations et dix fixtures | Parcours complet fusionné sur `main` |
| J3 · 17 octobre | SamaA11y en 4 h max, corrections WCAG et CI | Tests métier, Swagger, erreurs/timeouts et logs IA | CI verte, CLI produit JSON + Markdown |
| J4 · 18 octobre | Recette Docker et vidéo ≤ 3 min | Stabilisation, prompts et documentation | Démo reproductible ; décision bonus à 10 h |
| J5 · 19 octobre | Clone vierge, recette, gel à 12 h | Relecture API/README et livraison | Dépôt public, docs et vidéo prêts à soumettre |

Heures de coordination : UTC / Atlantic-Reykjavik, à distinguer de l'heure limite officielle
du formulaire de soumission, à vérifier par l'équipe.

## Dépendances et coupe-circuits

- Les tests utilisent des mocks explicites fondés sur OpenAPI. Seul le dashboard peut afficher
  cinq exemples automatiquement lors d'une indisponibilité initiale, avec bandeau permanent.
  L'analyse et la confirmation ne sont jamais simulées.
- Le contrat est gelé avant séparation. Une correction passe par accord et PR commune.
- J2 constitue la première fusion métier, pas une attente jusqu'au code freeze.
- La CLI SamaA11y reste à 150 lignes de logique métier maximum, sans UI ni base.
- Les explications IA dans SamaA11y ne sont envisagées le 18 à 10 h que si J1-J3 sont validés ;
  timebox 2 h. Sinon, priorité aux tests et au README.
- Aucun succès fictif pour contourner une panne fournisseur. Aucun scope creep auth/RAG/routing.

## Livraison

La publication du code sur GitHub n'est pas un déploiement de l'application.
Le socle doit démarrer sans clé ; la recette finale du parcours IA doit aussi être exécutée
avec une vraie clé et le modèle déclaré. Ne pas confondre mode démonstration et preuve IA.

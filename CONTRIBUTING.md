# Contribuer à SamaSignal AI

## Démarrer depuis le socle commun

Minela travaille sur `feature/frontend-vue`. Dev B crée lui-même sa branche après publication :

```bash
git clone git@github.com:Minela-WKM/samasignal-ai.git
cd samasignal-ai
git fetch origin
git switch -c feature/backend-spring origin/main
git push -u origin feature/backend-spring
```

Ne pas refaire `git init` ou `git remote add origin` dans un clone existant.
Publier chaque branche dès sa création ; le workflow CI écoute tous les push de branches.

## PR et synchronisation

1. Développer un lot court et cohérent ; vérifier les commandes du README.
2. Ouvrir une PR vers `main` avec comportement livré, tests et limites connues.
3. L'autre développeur relit ; l'auteur fusionne après approbation et CI verte.
4. Réintégrer `main` après chaque fusion :

```bash
git fetch origin
git merge origin/main
git push
```

Pas de force-push sur `main`. Conserver un historique lisible avec des commits `feat:`,
`fix:`, `docs:` ou `chore:`. Les branches doivent se recroiser avant la fin J2, le 16 octobre.
Ces règles sont des conventions ; leur protection effective sur GitHub doit être configurée
par le propriétaire du dépôt si elle est souhaitée.

## Zones de responsabilité

| Zone | Pilote |
| --- | --- |
| `apps/web`, `tools/sama-a11y`, Compose et workflow CI | Dev A |
| `apps/api`, Dockerfile API, fixtures métier, documentation Swagger | Dev B |
| Contrat API et décisions d'architecture | Accord des deux avant modification |

Toute modification de nom, enum, route ou réponse passe d'abord par une PR de contrat.
Mettre à jour OpenAPI, DTO Java, client frontend et exemples dans la même évolution coordonnée.
Dev B fournit les variables et le healthcheck nécessaires à l'intégration Docker de Dev A.

## Secrets et données

Ne jamais versionner `.env`, clés, tokens, photos privées, sorties de build ou journaux IA bruts.
Les clés sont exclusivement côté backend. Les tests CI restent déterministes et sans secret.
Ne pas exposer `AIExtractionLog` dans une route publique.

## Definition of Done d'une PR

- Le changement annoncé est réel ; les fonctionnalités futures sont indiquées comme telles.
- Tests pertinents et builds passent ; README/contrat reflètent la modification.
- Aucun renommage du contrat sans coordination ; aucun secret dans le diff.
- Pour une UI : clavier, focus visible, erreurs compréhensibles et états de chargement vérifiés.

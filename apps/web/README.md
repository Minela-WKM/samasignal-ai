# Frontend — Dev A / Minela

Branche : `feature/frontend-vue`. Vue 3, JavaScript Composition API, Tailwind, Leaflet, Node 24.

```bash
npm ci
npm run dev
npm test
npx playwright install chromium
npm run test:e2e
npm run build
```

Le frontend propose deux vues conservées en mémoire avec KeepAlive. Le header fixe réserve
sa hauteur réelle, la navigation place le focus sur le titre de la vue. Aucun texte ni photo
n'est conservé dans localStorage. Le rechargement de la page efface le brouillon local.

## Parcours citoyen

Description (1 à 5 000 caractères) et photo facultative (JPEG/PNG/WebP, 5 Mo), puis extraction
réelle par `/api/reports/analyze`. L'utilisateur corrige la catégorie, le résumé, l'urgence,
le lieu et les coordonnées, vérifie la fiche et confirme via `/api/reports/{id}/confirm`.
Le succès ne s'affiche qu'après réponse serveur. Aucun résultat IA ni confirmation n'est simulé.

La carte accepte un clic ; latitude et longitude sont aussi éditables au clavier. La
position du navigateur est demandée uniquement sur clic et son refus n'empêche pas la saisie.
Les requêtes ont un délai de 35 secondes pour l'analyse et 15 secondes pour liste/confirmation.
Aucun POST n'est relancé automatiquement : en cas de délai dépassé, la saisie est conservée.

## Supervision et démonstration

La liste complète `/api/reports` est filtrée localement avec ET, dans l'ordre serveur. Tableau,
compteur et carte utilisent les mêmes données. Les coordonnées invalides restent visibles dans
le tableau, sans marqueur. Les popups utilisent du texte échappé, jamais du HTML fourni par l'API.

Premier chargement réseau/timeout/404/5xx : cinq exemples synthétiques de Dakar sont affichés
avec un bandeau permanent « Mode démonstration — données fictives, API indisponible ».
Une liste vide, une réponse malformée ou un refus 401/403 ne déclenchent pas ce repli.
Après un succès réel, les pannes conservent les données réelles et les signalent comme obsolètes.
Une confirmation citoyenne récente empêche aussi le passage aux exemples : son succès reste
acquis même si le rechargement de la liste échoue. Le bouton Réessayer remplace les exemples
par la réponse réelle, sans mélange des sources.

## Contrat

`scoreConfiance` reste compris entre 0 et 1 et devient un pourcentage à l'affichage.
Les valeurs `haute/moyenne/basse` sont affichées CRITIQUE/MOYEN/FAIBLE ; `eau_usee` devient
ASSAINISSEMENT sans changer la valeur envoyée. Voir [OpenAPI](../../docs/openapi.yaml).
Le client valide les formes de réponse et les erreurs sans exposer les journaux IA.
Vite (local) et nginx (Docker) relaient les requêtes en même origine. Aucun secret côté client.

## Vérifications

Les tests Node couvrent filtres, validations, contrat et délais. Les tests Playwright couvrent
le parcours, les échecs API, la démo signalée, la carte, le clavier, le format mobile et axe-core.
API et tuiles sont interceptées avec des fixtures synthétiques ; ces tests ne prouvent pas
l'intégration réelle avec Dev B. Les screenshots de test montrent un fond de carte neutre simulé.

La [CLI SamaA11y](../../tools/sama-a11y/README.md) produit JSON/Markdown et screenshots pour
saisie, correction et dashboard. Après installation de ses dépendances et démarrage Vite :

```bash
npm run test:a11y
```

Contrastes vérifiés sur couleurs calculées : texte UI > 4,5:1, badges >= 7:1. Le tableau est
l'alternative textuelle au fond OSM, dont tous les détails graphiques ne sont pas contrôlés.
Les tests automatisés ne remplacent pas la recette avec lecteur d'écran ni un audit RGAA.

## Intégration avec Dev B

Le backend de `main` est encore un socle sans routes métier. L'analyse/confirmation afficheront
donc une erreur jusqu'à leur livraison. Le dashboard peut être démontré immédiatement.
La recette texte/photo → IA réelle → confirmation → supervision doit être exécutée séparément
avec le backend de Dev B. Aucun changement backend n'est livré dans cette branche.

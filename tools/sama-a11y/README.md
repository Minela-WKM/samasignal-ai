# SamaA11y

CLI indépendante Node.js + Playwright + axe-core : audits automatisés WCAG 2.1 AA, rapports
JSON et Markdown, screenshots des états audités. Pas d'interface citoyenne, pas de base, pas d'IA.

## Installation et audit générique

Prérequis : Node 24, accès à l'URL auditée.

```bash
cd tools/sama-a11y
npm ci
npx playwright install chromium
node cli.mjs http://localhost:5173 --output ../../reports/sama-a11y
```

Linux/CI : `npx playwright install --with-deps chromium`.
Le navigateur est headless. Codes de sortie : 0 = aucune violation automatisée détectée,
1 = violations, 2 = erreur technique. Les erreurs sont aussi inscrites dans les rapports.
Le fichier JSON conserve les violations, cibles et contrôles `incomplete` à revoir manuellement.

## Parcours SamaSignal reproductible

Démarrer Vite depuis `apps/web`, puis lancer depuis `tools/sama-a11y` :

```bash
node cli.mjs http://127.0.0.1:5173 --prepare ../../apps/web/e2e/a11y-scenario.mjs --output ../../reports/sama-a11y
```

Ce scénario intercepte explicitement l'API et les tuiles. Il audite **saisie, correction,
dashboard** avec des données synthétiques, sans clé IA et sans appel aux tuiles publiques.
Le JSON indique `preparedScenario: true`. Les captures utilisent un fond cartographique simulé.
Les rapports CI sont téléchargeables dans l'artefact `sama-a11y`.

## Réutiliser sur une autre application

Sans `--prepare`, la CLI audite la page telle qu'elle est servie. Pour un parcours personnalisé,
fournir un module JavaScript local de confiance exportant `setup(page)` (optionnel) et
`states = [{ name, enter: async page => { ... } }]`. Chaque état est préparé puis audité.
Le module s'exécute avec les permissions de Node : n'utiliser que du code local vérifié.

La logique de CLI reste inférieure à 150 lignes. Les tests axe-core détectent une partie des
problèmes WCAG 2.1 AA ; ils ne constituent ni certification ni audit RGAA. Vérifier aussi
clavier, focus, annonces au lecteur d'écran et pertinence du contenu. Les contrastes des
badges >= 7:1 font l'objet d'un test navigateur spécifique dans le frontend.

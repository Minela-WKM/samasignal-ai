# SamaA11y — module prévu, pas encore implémenté

Pilote : Dev A. CLI indépendante Node.js + Playwright + axe-core, auditant `apps/web`.
Pas d'interface dans l'application citoyenne, pas de base de données.

Livraison J3 : commande documentée acceptant l'URL cible et le dossier de sortie, rapports
JSON et Markdown, code de sortie non nul sur violations WCAG 2.1 AA ou erreur technique.
Auditer les états formulaire, correction et dashboard, pas seulement la page d'accueil.

Timebox : 4 h, 150 lignes maximum de logique métier. Ajouter le job CI lorsque la CLI fonctionne.
Un audit axe-core ne constitue pas un audit RGAA complet ; compléter par une vérification clavier,
du focus et des messages d'erreur. Les explications IA sont un bonus conditionnel J4.

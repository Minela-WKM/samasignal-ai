# Mise en ligne sur Render après fusion

## État et condition de lancement

Cette préparation regroupe les PR backend #6 et frontend #1 dans une branche d’intégration.
Les changements de format ont été harmonisés : `/api/v2/signalements`, `dateCreation`,
`erreursChamps`. La fusion locale de préparation ne fusionne aucune PR sur GitHub.
Le déploiement attend la revue, la fusion dans `main`, la CI verte et la connexion au compte Render.
Aucun service Render n’est créé par le dépôt seul.

## Services préparés

`render.yaml` définit deux services Docker (`samasignal-web`, `samasignal-api`) et une base PostgreSQL 17
(`sma-signalement`). Les ressources sont dans la même région et le nom demandé pour la base est
`sma signalement`. Vérifier que Render accepte ce nom lors de la validation du Blueprint ; ne pas
remplacer le nom d’une base existante. L’application reçoit le nom effectif via `fromDatabase`.

Les plans `free` sont explicites pour ne pas sélectionner un plan payant par défaut.
Ces plans conviennent à la démonstration : les services peuvent dormir et PostgreSQL gratuit
expire après 30 jours. Pour un usage durable, choisir et approuver un plan adapté avant création.
Référence : https://render.com/docs/free.

Le frontend contacte l’API par son adresse publique HTTPS, car un service web gratuit ne reçoit
pas de trafic réseau privé. L’adresse est reliée dynamiquement via `PUBLIC_API_HOST` ; aucune
adresse `onrender.com` supposée n’est codée en dur. Le proxy transmet l’hôte et le protocole
publics pour permettre l’accès à Swagger depuis le frontend.

## Procédure après fusion

1. Vérifier que le commit intégré est sur `main` et que tous les jobs CI sont verts.
2. Dans Render, connecter le dépôt puis créer un Blueprint depuis `render.yaml`, branche `main`.
3. Examiner la validation Render, les noms et les plans avant de confirmer la création.
4. Attendre la création de PostgreSQL et le succès du contrôle `/actuator/health` de l’API.
   Flyway crée les tables ; photos et journaux sont stockés dans PostgreSQL.
5. Vérifier que `PUBLIC_API_HOST` correspond à l’hôte public réel de l’API et que le frontend
   reçoit la même valeur dans `API_HOST`. Attendre le contrôle `/healthz` du frontend.
6. L’IA est initialement désactivée : saisir `GEMINI_API_KEY` dans l’environnement sécurisé
   de l’API, vérifier l’accès au modèle, puis passer `AI_MODE` à `hosted` et redéployer l’API.
   Aucun secret ne doit être enregistré dans Git ou transmis au navigateur.
7. Exécuter la recette réelle ci-dessous, puis conserver les URL effectivement attribuées.

## Recette de publication

- Ouvrir le frontend, vérifier le formulaire et la carte sans erreur de navigateur.
- Via l’URL frontend, consulter `/actuator/health`, `/swagger-ui.html` et `/v3/api-docs`.
- Créer un signalement réel texte/photo, compléter les champs et confirmer.
- Vérifier que la fiche apparaît dans la liste et sur la carte ; tester les filtres.
- Vérifier qu’un brouillon non confirmé reste absent de la liste publique.
- Consulter la photo puis redémarrer l’API et vérifier la persistance de la fiche et de la photo.
- Vérifier l’affichage des erreurs de validation et du délai IA sans perte de saisie.
- Vérifier les journaux d’extraction côté base sans les exposer par une adresse publique.
- Exécuter SamaA11y contre l’URL déployée ; les tests synthétiques ne prouvent pas la recette réelle.

## Vérifications de préparation

Les tests navigateur existants interceptent les réponses de l’API. Ils vérifient le comportement
frontend, pas un appel au modèle ou une persistance distante. La suite backend vérifie séparément
HTTP et JPA. La validation YAML/JSON Schema ne prouve ni l’éligibilité du compte aux plans,
ni l’acceptation du nom de base, ni l’attribution des noms de services par Render.
La publication n’est terminée qu’après validation Render et recette sur les URL réelles.

Références officielles :
- https://render.com/docs/blueprint-spec
- https://render.com/docs/docker
- https://render.com/docs/private-network

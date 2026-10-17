# Déployer SamaSignal AI avec Render Blueprints

## 1. Architecture retenue

Un Blueprint est une recette d'infrastructure : Render lit `render.yaml` dans GitHub et
crée les services, leurs variables et leurs connexions. Il ne lance pas `docker-compose.yml`,
qui reste destiné au développement local.

```text
Navigateur → HTTPS → samasignal-web (Vue compilé + Nginx)
                              ↓ proxy HTTPS /api/...
                       samasignal-api (Spring Boot / Java 21)
                              ↓ réseau privé
                       sma-signalement (PostgreSQL 17)
```

| Ressource | Source | Rôle / contrôle de santé |
| --- | --- | --- |
| `samasignal-web` | `apps/web/Dockerfile` ; contexte `apps/web` | Compile Vue avec Node 24, sert le site et relaie l'API ; `/healthz` |
| `samasignal-api` | `apps/api/Dockerfile` ; contexte `apps/api` | Compile Java avec Maven, démarre Spring Boot ; `/actuator/health` |
| `sma-signalement` | Bloc `databases` de `render.yaml` | Données, photos et journaux ; tables créées par Flyway au démarrage de l'API |

Les deux applications sont des **Web Services Docker**. Conserver cette architecture pour
le premier déploiement : le frontend utilise des URL relatives et dépend du proxy Nginx.
Le convertir simplement en Static Site supprimerait ce proxy.

Render fournit les identifiants PostgreSQL via `fromDatabase`. Le profil Spring `render`
construit une URL JDBC à partir de ces valeurs. Aucun mot de passe à copier depuis `.env`.
Le nom SQL n'est pas imposé : Render le génère pour une nouvelle base, et l'API reçoit
le nom effectif. Une base existante ne doit pas être renommée.

## 2. Publier la branche de préparation

Les modifications se font sur `feature/render-deployment`, jamais directement sur `main`.
Depuis la racine du dépôt, vérifier les changements puis les publier :

```powershell
git branch --show-current
git diff --check
git diff -- render.yaml docs/deploiement-render.md README.md
git add render.yaml docs/deploiement-render.md README.md
git commit -m "chore: simplify Render Blueprint deployment"
git push -u origin feature/render-deployment
```

La première commande doit afficher `feature/render-deployment`. Attendre le succès des jobs
GitHub Actions du commit publié : `web`, `accessibility`, `api` et `compose`.
Le réglage `autoDeployTrigger: checksPass` conditionne les déploiements automatiques aux checks ;
il ne remplace pas la vérification du commit avant une création ou un déploiement manuel.

## 3. Créer le Blueprint dans Render

Dans [Render Dashboard](https://dashboard.render.com), choisir **New → Blueprint** :

1. Connecter GitHub et sélectionner `Minela-WKM/samasignal-ai`.
2. Donner au Blueprint un nom, par exemple `samasignal-demo`.
3. Choisir la branche **`feature/render-deployment`**.
4. Indiquer **`render.yaml`** comme Blueprint Path (à la racine).
5. Examiner les ressources proposées : deux Web Services Docker et PostgreSQL 17,
   région `frankfurt`, plans `free`.
6. Cliquer sur **Deploy Blueprint** après validation du récapitulatif.

Ne pas ajouter `apps/web` comme racine globale : chaque service a déjà son Dockerfile et son
contexte dans le YAML. Aucune commande Build ou Start n'est à saisir : les Dockerfiles s'en chargent.
Les champs `branch` sont volontairement absents ; les deux services héritent de la branche
du Blueprint puisqu'ils utilisent le même dépôt.

**Si des ressources existent déjà :** vérifier le récapitulatif avant création. Pour gérer
une installation existante, partir de son Blueprint ou utiliser **Generate Blueprint** depuis
les ressources sélectionnées et comparer les paramètres. Ne pas attacher une ressource à deux
Blueprints, ni créer une seconde base par inadvertance. Garder région et nom SQL existants.

## 4. Vérifier le démarrage

Attendre la disponibilité de PostgreSQL puis le démarrage de l'API et du frontend.
Utiliser les URL réellement affichées par Render, sans déduire leur sous-domaine du nom du service.

| Où vérifier | Résultat attendu |
| --- | --- |
| API : `/actuator/health` | HTTP 200, JSON avec `status: UP` ; application et base disponibles |
| Frontend : `/healthz` | HTTP 200, `ok` ; Nginx disponible |
| Frontend : `/actuator/health` | `UP` ; proxy vers l'API fonctionnel |
| Frontend : `/api/v2/signalements` | JSON de la liste, éventuellement `[]` sur une base neuve |
| Frontend : `/swagger-ui/index.html` | Documentation interactive de l'API |

Le frontend reçoit automatiquement `API_HOST` directement depuis `RENDER_EXTERNAL_HOSTNAME`
de l'API. Ne pas passer par une variable intermédiaire définie avec `fromService` : Render
refuse de référencer une variable qui est elle-même une référence.
La valeur est un **nom d'hôte sans `https://` ni chemin** ; `API_SCHEME=https` fournit le protocole.
L'API utilise son adresse publique HTTPS car les Web Services gratuits ne reçoivent pas de
trafic entrant privé. PostgreSQL reste accessible par sa connexion privée ; `ipAllowList: []`
ferme son accès externe.

Exemple de vérification PowerShell, après remplacement de l'URL :

```powershell
$webUrl = 'https://REMPLACER-PAR-URL-FRONTEND.onrender.com'
Invoke-RestMethod "$webUrl/healthz"
Invoke-RestMethod "$webUrl/actuator/health"
Invoke-RestMethod "$webUrl/api/v2/signalements"
```

Une carte affichée n'est pas une preuve de connexion : le frontend peut montrer des données
synthétiques si le premier chargement échoue. Vérifier aussi la réponse JSON de l'API.

## 5. Activer l'analyse IA

Le Blueprint démarre avec **`AI_MODE=disabled`**. Cela permet de vérifier l'infrastructure sans
clé, mais l'analyse renverra `503 / IA_INDISPONIBLE` : le parcours de création complet n'est
pas opérationnel tant que l'IA n'est pas configurée.

1. Dans Render, ouvrir **samasignal-api → Environment**.
2. Ajouter `GEMINI_API_KEY` avec la clé du fournisseur, puis enregistrer.
3. Sur la branche de préparation, modifier `AI_MODE` de `disabled` vers `hosted` dans
   `render.yaml`, puis commit/push et synchroniser le Blueprint après CI verte.
4. Vérifier que le modèle indiqué par `GEMMA_MODEL` est accessible avec cette clé et accepte
   texte/photo. La valeur actuelle du dépôt est `gemma-4-26b-a4b-it` ; son accès réel reste à tester.
5. Attendre le redéploiement de l'API et tester une analyse réelle depuis le formulaire.

`GEMINI_API_KEY` reste exclusivement dans l'environnement serveur Render. Ne pas la placer
dans Git, `.env.example` ou une variable `VITE_*`. Les variables absentes du Blueprint sont
conservées lors d'une synchronisation. En revanche, modifier seulement `AI_MODE` dans le dashboard
serait temporaire : le YAML pourrait rétablir `disabled` à la synchronisation suivante.

## 6. Recette avant partage

- Créer un signalement texte/photo avec l'IA activée, corriger les champs et confirmer.
- Vérifier la présence dans la liste et sur la carte, puis tester les filtres.
- Vérifier qu'un brouillon non confirmé n'apparaît pas dans la liste publique.
- Redémarrer l'API et vérifier que la fiche et la photo persistent.
- Vérifier les erreurs de saisie et les délais IA sans perte du formulaire.

Les tests navigateur actuels utilisent des réponses API simulées. Ils ne prouvent pas l'accès
au fournisseur IA ni la persistance sur Render. Un healthcheck vert ne teste pas non plus l'IA.

## 7. Dépannage rapide

| Symptôme | Action |
| --- | --- |
| Blueprint introuvable | Vérifier que la branche a été poussée et que le chemin est `render.yaml` |
| Mauvais code déployé | Comparer la branche et le SHA du déploiement avec le commit GitHub attendu |
| Déploiement automatique en attente | Vérifier les checks GitHub du commit et les événements Render |
| API non saine | Lire ses logs : connexion PostgreSQL, variables `POSTGRES_*`, migrations Flyway, mémoire |
| Frontend vivant mais API en 502/504 | Tester l'URL API directement ; vérifier `API_HOST`, HTTPS et le réveil de l'API |
| `IA_INDISPONIBLE` | Vérifier `AI_MODE=hosted`, clé serveur, accès au modèle et quota fournisseur |
| Photos rejetées | Limite fichier Spring : 5 Mo ; requête totale Spring/Nginx : 6 Mo |
| Premier accès lent | Les services gratuits se mettent en veille ; attendre leur réveil puis réessayer |
| Synchronisation qui annule un réglage | Modifier la valeur dans le YAML si elle y est définie |

En cas d'arrêt mémoire du backend, examiner les logs avant d'ajuster le plan. Le Blueprint limite
déjà le heap Java à 65 % de la RAM ; cela ne garantit pas que toute la JVM tienne dans 512 Mo.

## 8. Gratuité et passage vers main

Les plans gratuits sont adaptés à une démonstration. Render indique une mise en veille après
15 minutes d'inactivité et **750 heures gratuites partagées par workspace et par mois** : les
deux services consomment ce même quota. PostgreSQL gratuit expire après **30 jours**.
Prévoir un plan durable et une sauvegarde avant cette échéance si les données doivent être conservées.

Après recette : ouvrir une PR vers `main`, obtenir la revue et attendre la CI verte avant fusion.
Pour déployer ensuite depuis `main`, changer la branche liée au Blueprint et vérifier que les
deux services suivent bien `main` dans le plan de synchronisation. Conserver les mêmes ressources
si l'objectif est de garder les données ; ne pas recréer le Blueprint pour un simple changement de branche.

## Sources officielles

- [Créer et gérer un Blueprint](https://render.com/docs/infrastructure-as-code)
- [Référence YAML : branches, Docker, variables et bases](https://render.com/docs/blueprint-spec)
- [Limites des offres gratuites](https://render.com/docs/free)
- [Réseau privé](https://render.com/docs/private-network)

La validation finale appartient à Render : elle dépend aussi de l'état du compte et des ressources
existantes. Ce guide prépare le déploiement ; il ne constitue pas une preuve de publication réussie.

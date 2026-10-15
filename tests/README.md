# Recette transverse

Les tests exécutables actuels sont colocalisés dans `apps/api/src/test` et `apps/web/test`.
La CI complète ces tests par un démarrage Compose avec PostgreSQL et vérifie le reverse proxy.

Scénarios E2E à ajouter quand les routes métier existent :

1. Texte et photo → extraction → correction → confirmation → visible dans liste et carte.
2. Texte seul, lieu absent → complétion obligatoire avant confirmation.
3. Refus de géolocalisation → point choisi manuellement, sans blocage du parcours.
4. Photo invalide/trop lourde, JSON IA invalide, timeout → erreur explicite, saisie conservée.
5. Filtres catégorie + urgence ; brouillons absents du dashboard ; confirmation répétée sans doublon.
6. Redémarrage Compose → fiches et photos conservées.
7. Navigation clavier, focus et annonces accessibles des erreurs/chargements.

Utiliser des fixtures synthétiques et des réponses IA déterministes en CI. La preuve d'appel
Gemma réel est une recette séparée, jamais un secret requis pour les PR de contributeurs.

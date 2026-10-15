# Frontend — Dev A / Minela

Branche : `feature/frontend-vue`. Vue 3, Vite, Tailwind, JavaScript ESM, Node 24.

```bash
npm ci
npm run dev
npm test
npm run build
```

Le socle affiche une page d'accueil et vérifie `/actuator/health`. Il n'appelle pas encore les
routes métier. Le client commun `src/lib/api.js` suit [le contrat](../../docs/openapi.yaml).
Le proxy Vite et le proxy nginx utilisent la même origine ; aucun secret n'est nécessaire au frontend.

Prochain lot : formulaire texte/photo, correction des champs manquants, sélection Leaflet du
point (géolocalisation facultative), confirmation et dashboard liste/carte filtrable.
Ajouter Leaflet lors de ce lot, avec attribution OpenStreetMap et une alternative accessible
au clavier pour la saisie des coordonnées. Pas de login ni de faux bouton d'envoi dans le socle.

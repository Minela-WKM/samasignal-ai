// Synthetic demonstration fixtures. Never sent to analyze/confirm.
export const demoReports = [
  ['medina', 'dechet', 'Déchets accumulés près du marché de Médina.', 'Médina, Dakar', 'haute', 14.6838, -17.4512, 0.94],
  ['liberte', 'eclairage', 'Lampadaire éteint à proximité du rond-point.', 'Liberté 6, Dakar', 'moyenne', 14.7305, -17.4658, 0.88],
  ['parcelles', 'eau_usee', 'Écoulement d’eaux usées sur la voie piétonne.', 'Parcelles Assainies, Dakar', 'haute', 14.7596, -17.4380, 0.91],
  ['plateau', 'voirie', 'Nid-de-poule sur une chaussée fréquentée.', 'Plateau, Dakar', 'moyenne', 14.6697, -17.4332, 0.86],
  ['ouakam', 'autre', 'Mobilier urbain endommagé près d’un arrêt.', 'Ouakam, Dakar', 'basse', 14.7242, -17.4892, 0.79],
].map(([id, categorie, resume, localisationDeclaree, niveauUrgence, latitude, longitude, scoreConfiance], i) => ({
  id: `demo-${id}`, texteBrut: resume, photoUrl: null, categorie, resume, localisationDeclaree,
  niveauUrgence, latitude, longitude, scoreConfiance, champsManquants: [], elementsVisuelsDetectes: [],
  statut: 'CONFIRME', dateCreation: `2026-10-16T0${9 - i}:00:00Z`,
}))

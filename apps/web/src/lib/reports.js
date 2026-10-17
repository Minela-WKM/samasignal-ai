export const categories = { voirie: 'VOIRIE', eau_usee: 'ASSAINISSEMENT', eclairage: 'ÉCLAIRAGE', dechet: 'DÉCHETS', autre: 'AUTRE' }
export const urgencies = { haute: 'CRITIQUE', moyenne: 'MOYEN', basse: 'FAIBLE' }
export const confidence = (value) => Number.isFinite(value) && value >= 0 && value <= 1 ? `${Math.round(value * 100)} %` : 'Indisponible'
export const hasPosition = (report) => Number.isFinite(report.latitude) && Number.isFinite(report.longitude) && Math.abs(report.latitude) <= 90 && Math.abs(report.longitude) <= 180
export const filterReports = (reports, category, urgency) => reports.filter(r => (!category || r.categorie === category) && (!urgency || r.niveauUrgence === urgency))
export const canUseDemo = (error) => ['NETWORK_ERROR', 'TIMEOUT'].includes(error.code) || error.status === 404 || error.status >= 500

export function validatePhoto(file) {
  if (!file) return ''
  if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) return 'Choisissez une photo JPEG, PNG ou WebP.'
  return file.size > 5 * 1024 * 1024 ? 'La photo dépasse 5 Mo.' : ''
}
export function validateConfirmation(values) {
  const errors = {}
  if (!Object.hasOwn(categories, values.categorie)) errors.categorie = 'Choisissez une catégorie.'
  if (!values.resume?.trim() || values.resume.length > 1000) errors.resume = 'Saisissez un résumé de 1 à 1 000 caractères.'
  if (!values.localisationDeclaree?.trim() || values.localisationDeclaree.length > 500) errors.localisationDeclaree = 'Indiquez le lieu (500 caractères maximum).'
  if (!Object.hasOwn(urgencies, values.niveauUrgence)) errors.niveauUrgence = 'Choisissez une urgence.'
  for (const [key, limit] of [['latitude', 90], ['longitude', 180]]) {
    if (values[key] === '' || values[key] == null || !Number.isFinite(Number(values[key])) || Math.abs(Number(values[key])) > limit) errors[key] = `Saisissez une ${key} entre -${limit} et ${limit}.`
  }
  return errors
}
export function assertReport(value, expectedStatus) {
  const invalid = !value || typeof value.id !== 'string' || !value.id ||
    typeof value.texteBrut !== 'string' || typeof value.resume !== 'string' || !value.resume.trim() ||
    !(value.localisationDeclaree === null || typeof value.localisationDeclaree === 'string') ||
    !Object.hasOwn(categories, value.categorie) || !Object.hasOwn(urgencies, value.niveauUrgence) ||
    !['EN_ATTENTE', 'CONFIRME', 'TRAITE'].includes(value.statut) ||
    (expectedStatus && value.statut !== expectedStatus) ||
    !Number.isFinite(value.scoreConfiance) || value.scoreConfiance < 0 || value.scoreConfiance > 1 ||
    !Array.isArray(value.champsManquants) || !value.champsManquants.every(s => typeof s === 'string') ||
    !Array.isArray(value.elementsVisuelsDetectes) || !value.elementsVisuelsDetectes.every(s => typeof s === 'string') ||
    typeof value.dateCreation !== 'string' || !Number.isFinite(Date.parse(value.dateCreation)) ||
    (expectedStatus === 'CONFIRME' && (
      value.champsManquants.length > 0 ||
      !hasPosition(value) ||
      typeof value.localisationDeclaree !== 'string' ||
      !value.localisationDeclaree.trim()
    ))
  if (invalid) throw Object.assign(new Error('La réponse du serveur ne respecte pas le contrat attendu.'), { code: 'INVALID_RESPONSE' })
  return value
}
export function assertReportList(value) {
  if (!Array.isArray(value)) throw Object.assign(new Error('La liste reçue est invalide.'), { code: 'INVALID_RESPONSE' })
  const ids = new Set()
  for (const report of value) {
    assertReport(report)
    if (report.statut === 'EN_ATTENTE' || ids.has(report.id)) throw Object.assign(new Error('La liste contient une fiche non publique ou un doublon.'), { code: 'INVALID_RESPONSE' })
    ids.add(report.id)
  }
  return value
}

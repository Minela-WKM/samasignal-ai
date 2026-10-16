import { demoReports } from '../src/fixtures/demoReports.js'
export const records = demoReports.map((r, i) => ({ ...r, id: `72b6d99d-fb95-4dac-bb2d-38d81878540${i}` }))
export const draft = { ...records[0], localisationDeclaree: null, latitude: null, longitude: null, statut: 'EN_ATTENTE', champsManquants: ['localisationDeclaree', 'latitude', 'longitude'] }
const blankTile = Buffer.from('iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQIHWP4z8DwHwAFgAI/ScLbtAAAAABJRU5ErkJggg==', 'base64')
export async function stubTiles(page) {
  await page.route('https://tile.openstreetmap.org/**', route => route.fulfill({ contentType: 'image/png', body: blankTile }))
}
export async function mockApi(page) {
  await stubTiles(page)
  await page.route('**/api/reports', route => route.fulfill({ json: records }))
  await page.route('**/api/reports/analyze', route => route.fulfill({ status: 201, json: draft }))
  await page.route('**/api/reports/*/confirm', route => route.fulfill({ json: { ...draft, ...route.request().postDataJSON(), statut: 'CONFIRME', champsManquants: [] } }))
}
export async function enterCorrection(page) {
  await page.getByLabel('Que se passe-t-il ?').fill('Des déchets encombrent le trottoir près du marché de Médina.')
  await page.getByRole('button', { name: 'Analyser mon signalement' }).click()
  await page.getByRole('heading', { name: 'Vérifiez la fiche proposée' }).waitFor()
}
export async function completeCorrection(page) {
  await page.getByLabel('Lieu du signalement', { exact: true }).fill('Médina, Dakar')
  await page.getByLabel('Latitude', { exact: true }).fill('14.6838')
  await page.getByLabel('Longitude', { exact: true }).fill('-17.4512')
  await page.getByRole('button', { name: 'Vérifier avant confirmation' }).click()
}

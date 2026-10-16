import { mockApi, enterCorrection } from './fixtures.js'
export const setup = mockApi
export const states = [
  { name: 'saisie', enter: async () => {} },
  { name: 'correction', enter: enterCorrection },
  { name: 'dashboard', enter: async page => {
    await page.getByRole('button', { name: 'Supervision Mairie', exact: true }).click()
    await page.getByRole('status').filter({ hasText: '5 signalements affichés' }).waitFor()
  } },
]

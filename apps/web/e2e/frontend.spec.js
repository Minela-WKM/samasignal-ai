import { test, expect } from '@playwright/test'
import { mockApi, stubTiles, records, draft, enterCorrection, completeCorrection } from './fixtures.js'

test('citizen journey with photo, human correction and real confirmation response', async ({ page }) => {
  await mockApi(page)
  let analyzed = false, confirmations = 0
  await page.route('**/api/v2/signalements/analyser', async route => {
    const body = route.request().postDataBuffer().toString()
    expect(body).toContain('name="texteBrut"'); expect(body).toContain('name="photo"')
    analyzed = true
    await route.fulfill({ status: 201, json: draft })
  })
  await page.route('**/api/v2/signalements/*/confirmer', async route => {
    confirmations++
    const body = route.request().postDataJSON()
    expect(Object.keys(body).sort()).toEqual(['categorie', 'resume', 'localisationDeclaree', 'niveauUrgence', 'latitude', 'longitude'].sort())
    expect(body.latitude).toBe(14.6838)
    await route.fulfill({ json: { ...draft, ...body, statut: 'CONFIRME', champsManquants: [] } })
  })
  await page.goto('/')
  await page.getByLabel('Ajouter une photo').setInputFiles({ name: 'photo.png', mimeType: 'image/png', buffer: Buffer.from('test photo') })
  await enterCorrection(page)
  await page.getByRole('button', { name: 'Vérifier avant confirmation' }).click()
  await expect(page.getByLabel('Lieu du signalement', { exact: true })).toHaveAttribute('aria-invalid', 'true')
  await completeCorrection(page)
  await page.getByRole('button', { name: 'Confirmer le signalement', exact: true }).click()
  await expect(page.getByRole('heading', { name: 'Signalement confirmé', exact: true })).toBeVisible()
  await page.getByRole('button', { name: 'Voir dans la supervision' }).click()
  await expect(page.locator('tr[data-selected="true"]')).toHaveCount(1)
  await expect(page.getByRole('table')).toBeVisible()
  expect(analyzed).toBe(true); expect(confirmations).toBe(1)
})

test('view switches preserve draft, filters and move focus to the new title', async ({ page }) => {
  await mockApi(page); await page.goto('/')
  await page.getByLabel('Que se passe-t-il ?').fill('Mon brouillon')
  await page.getByRole('button', { name: 'Supervision Mairie', exact: true }).click()
  await expect(page.getByRole('heading', { level: 1 })).toBeFocused()
  await page.getByLabel('Catégorie', { exact: true }).selectOption('dechet')
  await expect(page.getByRole('status').filter({ hasText: '1 signalement affiché' })).toBeVisible()
  await page.getByRole('button', { name: 'Signalement Citoyen', exact: true }).click()
  await expect(page.getByLabel('Que se passe-t-il ?')).toHaveValue('Mon brouillon')
  await page.getByRole('button', { name: 'Supervision Mairie', exact: true }).click()
  await expect(page.getByLabel('Catégorie', { exact: true })).toHaveValue('dechet')
})

test('five demo records on unavailable API, filters synchronize table and map, then real recovery', async ({ page }) => {
  await stubTiles(page)
  await page.route('**/api/v2/signalements', route => route.fulfill({ status: 503, json: { message: 'Indisponible' } }))
  await page.goto('/'); await page.getByRole('button', { name: 'Supervision Mairie', exact: true }).click()
  await expect(page.getByText('Mode démonstration — données fictives, API indisponible', { exact: true })).toBeVisible()
  await expect(page.locator('tbody tr')).toHaveCount(5)
  await expect(page.locator('.map-pin')).toHaveCount(5)
  await page.getByLabel('Urgence', { exact: true }).selectOption('haute')
  await expect(page.locator('tbody tr')).toHaveCount(2); await expect(page.locator('.map-pin')).toHaveCount(2)
  await page.getByRole('button', { name: /Localiser sur la carte/ }).first().click()
  await expect(page.getByRole('region', { name: 'Carte interactive des signalements citoyens à Dakar' })).toBeFocused()
  await expect(page.locator('.leaflet-popup-content')).toContainText('Urgence : CRITIQUE')
  await page.route('**/api/v2/signalements', route => route.fulfill({ json: [] }))
  await page.getByRole('button', { name: 'Réessayer l’API' }).click()
  await expect(page.getByText('Mode démonstration', { exact: false })).toHaveCount(0)
  await expect(page.getByText('Aucun signalement ne correspond à cette sélection.')).toBeVisible()
})

test('401 and malformed responses never produce synthetic data', async ({ page }) => {
  await stubTiles(page)
  await page.route('**/api/v2/signalements', route => route.fulfill({ status: 401, json: { message: 'Accès refusé' } }))
  await page.goto('/'); await page.getByRole('button', { name: 'Supervision Mairie', exact: true }).click()
  await expect(page.getByRole('alert')).toContainText('Accès refusé')
  await expect(page.getByText('Mode démonstration', { exact: false })).toHaveCount(0)
  await page.route('**/api/v2/signalements', route => route.fulfill({ json: { wrong: [] } }))
  await page.getByRole('button', { name: 'Actualiser les signalements' }).click()
  await expect(page.getByRole('alert')).toContainText('invalide')
  await expect(page.locator('.map-pin')).toHaveCount(0)
})

test('outage after real data keeps the real records, not fixtures', async ({ page }) => {
  await mockApi(page); await page.route('**/api/v2/signalements', route => route.fulfill({ json: [records[0]] }))
  await page.goto('/'); await page.getByRole('button', { name: 'Supervision Mairie', exact: true }).click()
  await expect(page.locator('tbody tr')).toHaveCount(1)
  await page.route('**/api/v2/signalements', route => route.abort('failed'))
  await page.getByRole('button', { name: 'Actualiser les signalements' }).click()
  await expect(page.getByRole('alert')).toContainText('obsolètes')
  await expect(page.locator('tbody tr')).toHaveCount(1)
  await expect(page.getByText('Mode démonstration', { exact: false })).toHaveCount(0)
})

test('missing coordinates stay in the table and HTML in a popup remains plain text', async ({ page }) => {
  await mockApi(page)
  const html = '<img src=x onerror=alert(1)>'
  await page.route('**/api/v2/signalements', route => route.fulfill({ json: [{ ...records[0], resume: html }, { ...records[1], latitude: null }] }))
  await page.goto('/'); await page.getByRole('button', { name: 'Supervision Mairie', exact: true }).click()
  await expect(page.locator('tbody tr')).toHaveCount(2); await expect(page.locator('.map-pin')).toHaveCount(1)
  await expect(page.getByText('Position indisponible')).toBeVisible()
  await page.getByRole('button', { name: /Localiser sur la carte/ }).click()
  await expect(page.locator('.leaflet-popup-content')).toContainText(html)
  await expect(page.locator('.leaflet-popup-content img')).toHaveCount(0)
})

test('geolocation refusal and tile failure allow manual completion', async ({ page }) => {
  await mockApi(page)
  await page.route('https://tile.openstreetmap.org/**', route => route.abort('failed'))
  await page.addInitScript(() => { navigator.geolocation.getCurrentPosition = (success, failure) => failure({ code: 1 }) })
  await page.goto('/'); await enterCorrection(page)
  await page.getByRole('button', { name: 'Utiliser ma position' }).click()
  await expect(page.getByText(/Position non obtenue/)).toBeVisible()
  await expect(page.getByText(/Le fond de carte est indisponible/)).toBeVisible()
  await completeCorrection(page)
  await expect(page.getByRole('heading', { name: 'Prêt à envoyer ?' })).toBeVisible()
})

test('pending analysis blocks duplicates; a failure preserves the input', async ({ page }) => {
  await mockApi(page)
  let calls = 0, release
  const gate = new Promise(resolve => { release = resolve })
  await page.route('**/api/v2/signalements/analyser', async route => { calls++; await gate; await route.fulfill({ status: 504, json: { code: 'IA_DELAI_DEPASSE', message: 'Analyse trop longue' } }) })
  await page.goto('/')
  await page.getByLabel('Que se passe-t-il ?').fill('Un lampadaire éteint')
  await page.getByRole('button', { name: 'Analyser mon signalement' }).click()
  await expect(page.getByRole('button', { name: 'Analyse en cours…' })).toBeDisabled()
  release()
  await expect(page.getByRole('alert')).toContainText('Analyse trop longue')
  await expect(page.getByLabel('Que se passe-t-il ?')).toHaveValue('Un lampadaire éteint')
  expect(calls).toBe(1)
})

test('confirmation errors remain editable and refresh failure cannot undo success', async ({ page }) => {
  await mockApi(page)
  await page.route('**/api/v2/signalements/*/confirmer', route => route.fulfill({ status: 400, json: { message: 'Lieu à préciser', erreursChamps: { localisationDeclaree: 'Ajoutez une rue.' } } }))
  await page.goto('/'); await enterCorrection(page); await completeCorrection(page)
  await page.getByRole('button', { name: 'Confirmer le signalement', exact: true }).click()
  await expect(page.getByLabel('Lieu du signalement', { exact: true })).toHaveAttribute('aria-invalid', 'true')
  await page.route('**/api/v2/signalements/*/confirmer', route => route.fulfill({ json: { ...draft, ...route.request().postDataJSON(), statut: 'CONFIRME', champsManquants: [] } }))
  await completeCorrection(page)
  await page.getByRole('button', { name: 'Confirmer le signalement', exact: true }).click()
  await page.route('**/api/v2/signalements', route => route.fulfill({ status: 503, json: {} }))
  await page.getByRole('button', { name: 'Voir dans la supervision' }).click()
  await expect(page.getByRole('alert')).toContainText('Votre confirmation reste enregistrée')
  await expect(page.getByText('Mode démonstration', { exact: false })).toHaveCount(0)
  await page.getByRole('button', { name: 'Signalement Citoyen', exact: true }).click()
  await expect(page.getByRole('heading', { name: 'Signalement confirmé', exact: true })).toBeVisible()
})

test('unsupported upload is explained and the image preview can be removed', async ({ page }) => {
  await mockApi(page); await page.goto('/')
  const input = page.getByLabel('Ajouter une photo')
  await input.setInputFiles({ name: 'bad.txt', mimeType: 'text/plain', buffer: Buffer.from('bad') })
  await expect(page.locator('#photo-error')).toContainText('JPEG')
  await input.setInputFiles({ name: 'good.png', mimeType: 'image/png', buffer: Buffer.from('image') })
  await expect(page.getByAltText('Aperçu de la photo jointe au signalement')).toBeVisible()
  await page.getByRole('button', { name: 'Retirer la photo' }).click()
  await expect(page.getByAltText('Aperçu de la photo jointe au signalement')).toHaveCount(0)
})

test('320px layout and 200% content scale do not obscure the page or overflow globally', async ({ page }) => {
  await mockApi(page); await page.setViewportSize({ width: 320, height: 900 }); await page.goto('/')
  await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
  await page.keyboard.press('Tab'); await expect(page.getByRole('link', { name: 'Aller au contenu' })).toBeFocused()
  await page.keyboard.press('Enter'); await expect(page.locator('main')).toBeFocused()
  for (const view of ['Supervision Mairie', 'Signalement Citoyen']) {
    await page.getByRole('button', { name: view, exact: true }).click()
    expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
  }
  await enterCorrection(page)
  expect(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth)).toBe(true)
  await page.setViewportSize({ width: 1280, height: 900 })
  await page.evaluate(() => { document.body.style.zoom = '2' })
  await page.evaluate(() => window.scrollTo(0, 0))
  await expect.poll(() => page.evaluate(() => document.querySelector('h1').getBoundingClientRect().top >= document.querySelector('header').getBoundingClientRect().bottom)).toBe(true)
})

test('client deadline preserves draft and does not automatically retry analysis', async ({ page }) => {
  await mockApi(page)
  await page.clock.install()
  let calls = 0, release
  const gate = new Promise(resolve => { release = resolve })
  await page.route('**/api/v2/signalements/analyser', async route => { calls++; await gate; await route.fulfill({ status: 201, json: draft }).catch(() => {}) })
  await page.goto('/')
  await page.getByLabel('Que se passe-t-il ?').fill('Un problème de voirie')
  await page.getByRole('button', { name: 'Analyser mon signalement' }).click()
  await expect(page.getByRole('button', { name: 'Analyse en cours…' })).toBeDisabled()
  await page.clock.fastForward(36000)
  await expect(page.getByRole('alert')).toContainText('délai de réponse')
  await expect(page.getByLabel('Que se passe-t-il ?')).toHaveValue('Un problème de voirie')
  release(); expect(calls).toBe(1)
})

test('pending confirmation cannot be submitted twice', async ({ page }) => {
  await mockApi(page)
  let calls = 0, release
  const gate = new Promise(resolve => { release = resolve })
  await page.route('**/api/v2/signalements/*/confirmer', async route => {
    calls++; await gate
    await route.fulfill({ json: { ...draft, ...route.request().postDataJSON(), statut: 'CONFIRME', champsManquants: [] } })
  })
  await page.goto('/'); await enterCorrection(page); await completeCorrection(page)
  await page.getByRole('button', { name: 'Confirmer le signalement', exact: true }).click()
  await expect(page.getByRole('button', { name: 'Confirmation en cours…' })).toBeDisabled()
  await expect(page.getByRole('button', { name: 'Corriger la fiche' })).toBeDisabled()
  release()
  await expect(page.getByRole('heading', { name: 'Signalement confirmé', exact: true })).toBeVisible()
  expect(calls).toBe(1)
})

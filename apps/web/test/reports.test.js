import { test } from 'node:test'
import assert from 'node:assert/strict'
import { categories, urgencies, confidence, hasPosition, filterReports, canUseDemo, validatePhoto, validateConfirmation, assertReport, assertReportList } from '../src/lib/reports.js'
import { demoReports } from '../src/fixtures/demoReports.js'

test('display labels never rename the wire values', () => {
  assert.equal(categories.eau_usee, 'ASSAINISSEMENT')
  assert.equal(urgencies.haute, 'CRITIQUE')
  assert.equal(confidence(0.94), '94 %')
  assert.equal(confidence(null), 'Indisponible')
  assert.equal(confidence(2), 'Indisponible')
})
test('combined filters retain source order and empty filters retain all records', () => {
  assert.equal(filterReports(demoReports, '', '').length, 5)
  assert.deepEqual(filterReports(demoReports, 'dechet', 'haute').map(r => r.id), ['demo-medina'])
  assert.equal(filterReports(demoReports, 'voirie', 'haute').length, 0)
})
test('coordinates distinguish zero from missing values and reject invalid ranges', () => {
  assert.equal(hasPosition({ latitude: 0, longitude: 0 }), true)
  for (const latitude of [null, '', '14', 91, NaN]) assert.equal(hasPosition({ latitude, longitude: 0 }), false)
})
test('confirmation requires location and a complete bounded coordinate pair', () => {
  const values = { categorie: 'voirie', niveauUrgence: 'basse', resume: 'Trou', localisationDeclaree: 'Dakar', latitude: 0, longitude: 0 }
  assert.deepEqual(validateConfirmation(values), {})
  assert.deepEqual(Object.keys(validateConfirmation({ ...values, localisationDeclaree: ' ', latitude: '', longitude: 181 })), ['localisationDeclaree', 'latitude', 'longitude'])
})
test('photo validation accepts the limit and rejects unsupported types', () => {
  assert.equal(validatePhoto({ type: 'image/png', size: 5 * 1024 * 1024 }), '')
  assert.match(validatePhoto({ type: 'image/png', size: 5 * 1024 * 1024 + 1 }), /5 Mo/)
  assert.match(validatePhoto({ type: 'text/html', size: 20 }), /JPEG/)
})
test('demo is eligible only for availability failures', () => {
  for (const error of [{ code: 'NETWORK_ERROR' }, { code: 'TIMEOUT' }, { status: 404 }, { status: 503 }]) assert.equal(canUseDemo(error), true)
  for (const error of [{ status: 401 }, { status: 403 }, { code: 'INVALID_RESPONSE' }, { code: 'ABORTED' }]) assert.equal(Boolean(canUseDemo(error)), false)
})
test('malformed lists, private drafts and duplicate IDs cannot enter the dashboard', () => {
  assert.equal(assertReportList(demoReports).length, 5)
  assert.deepEqual(assertReportList([]), [])
  for (const value of [{}, [{}], [demoReports[0], demoReports[0]], [{ ...demoReports[0], statut: 'EN_ATTENTE' }], [{ ...demoReports[0], scoreConfiance: 50 }]]) assert.throws(() => assertReportList(value), { code: 'INVALID_RESPONSE' })
})
test('confirmation completeness invariants are enforced when expectedStatus is CONFIRME', () => {
  const complete = demoReports[0]
  assert.equal(assertReport(complete, 'CONFIRME'), complete)

  // nonempty champsManquants
  assert.throws(() => assertReport({ ...complete, champsManquants: ['localisationDeclaree'] }, 'CONFIRME'), { code: 'INVALID_RESPONSE' })

  // missing or invalid coordinates
  for (const invalidCoords of [
    { latitude: null, longitude: -17.4512 },
    { latitude: 14.6838, longitude: null },
    { latitude: 91, longitude: -17.4512 },
    { latitude: 14.6838, longitude: 181 },
    { latitude: '14.6838', longitude: -17.4512 },
  ]) {
    assert.throws(() => assertReport({ ...complete, ...invalidCoords }, 'CONFIRME'), { code: 'INVALID_RESPONSE' })
  }

  // blank or missing location
  for (const localisationDeclaree of [null, '', '   ']) {
    assert.throws(() => assertReport({ ...complete, localisationDeclaree }, 'CONFIRME'), { code: 'INVALID_RESPONSE' })
  }

  // draft reports can have missing fields when expectedStatus is EN_ATTENTE
  const draft = { ...complete, statut: 'EN_ATTENTE', localisationDeclaree: null, latitude: null, longitude: null, champsManquants: ['latitude'] }
  assert.equal(assertReport(draft, 'EN_ATTENTE'), draft)
})


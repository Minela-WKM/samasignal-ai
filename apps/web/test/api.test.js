import { afterEach, test } from 'node:test'
import assert from 'node:assert/strict'
import { getHealth, reportsApi } from '../src/lib/api.js'

const originalFetch = globalThis.fetch
afterEach(() => { globalThis.fetch = originalFetch })

test('health checks the real API response', async () => {
  globalThis.fetch = async (url) => {
    assert.equal(url, '/actuator/health')
    return Response.json({ status: 'UP' })
  }
  assert.equal((await getHealth()).status, 'UP')
})

test('confirmation preserves contract and validation errors', async () => {
  globalThis.fetch = async (url, options) => {
    assert.equal(url, '/api/reports/123/confirm')
    assert.equal(options.method, 'POST')
    assert.deepEqual(JSON.parse(options.body), { categorie: 'voirie' })
    return Response.json({ code: 'VALIDATION_ERROR', message: 'Lieu requis', fieldErrors: {
      localisationDeclaree: 'Champ requis',
    } }, { status: 400 })
  }
  await assert.rejects(reportsApi.confirm('123', { categorie: 'voirie' }), (error) => {
    assert.equal(error.status, 400)
    assert.equal(error.code, 'VALIDATION_ERROR')
    assert.equal(error.fieldErrors.localisationDeclaree, 'Champ requis')
    return true
  })
})

test('filters use urgence and categorie, and omit empty selections', async () => {
  globalThis.fetch = async (url) => {
    assert.equal(url, '/api/reports?urgence=haute&categorie=eau_usee')
    return Response.json([])
  }
  assert.deepEqual(await reportsApi.list({ urgence: 'haute', categorie: 'eau_usee', unused: '' }), [])
})

test('a non-JSON upstream failure remains a failure, never a demo result', async () => {
  globalThis.fetch = async () => new Response('Bad gateway', { status: 502 })
  await assert.rejects(reportsApi.list(), { status: 502 })
})

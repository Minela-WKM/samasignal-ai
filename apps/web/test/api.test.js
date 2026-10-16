import { afterEach, test } from 'node:test'
import assert from 'node:assert/strict'
import { getHealth, reportsApi, request } from '../src/lib/api.js'

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

test('successful non-JSON responses are contract errors, not network failures', async () => {
  globalThis.fetch = async () => new Response('<html>Proxy error</html>')
  await assert.rejects(reportsApi.list(), { code: 'INVALID_RESPONSE' })
})

test('timeout aborts a request exactly once without retrying POST', async () => {
  let calls = 0
  globalThis.fetch = async (url, { signal }) => {
    calls++
    return new Promise((resolve, reject) => signal.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError'))))
  }
  await assert.rejects(request('/api/reports/analyze', { method: 'POST', timeoutMs: 10 }), { code: 'TIMEOUT' })
  assert.equal(calls, 1)
})

test('caller cancellation is distinct from a timeout', async () => {
  const controller = new AbortController()
  globalThis.fetch = async (url, { signal }) => new Promise((resolve, reject) => signal.addEventListener('abort', () => reject(new DOMException('Aborted', 'AbortError'))))
  const pending = request('/api/reports', { signal: controller.signal })
  controller.abort()
  await assert.rejects(pending, { code: 'ABORTED' })
})

import { assertReport, assertReportList } from './reports.js'
export async function request(path, { signal, timeoutMs = 15000, ...options } = {}) {
  const controller = new AbortController()
  let timedOut = false
  const abort = () => controller.abort()
  if (signal?.aborted) abort()
  else signal?.addEventListener('abort', abort, { once: true })
  const timer = setTimeout(() => { timedOut = true; controller.abort() }, timeoutMs)
  try {
    const baseUrl = import.meta.env?.VITE_API_URL || ''
    const response = await fetch(baseUrl + path, { ...options, signal: controller.signal })
    if (response.status === 204) return null
    let body
    try { body = await response.json() } catch {
      if (response.ok) throw Object.assign(new Error('Réponse JSON invalide du serveur.'), { code: 'INVALID_RESPONSE' })
    }
    if (!response.ok) throw Object.assign(new Error(body?.message || `Le service a répondu ${response.status}.`), {
      status: response.status, code: body?.code || 'HTTP_ERROR',
      fieldErrors: body?.erreursChamps && typeof body.erreursChamps === 'object' ? body.erreursChamps : {},
    })
    return body
  } catch (error) {
    if (timedOut) throw Object.assign(new Error('Le délai de réponse est dépassé. Votre saisie est conservée ; vous pouvez réessayer.'), { code: 'TIMEOUT' })
    if (signal?.aborted) throw Object.assign(new Error('Requête annulée.'), { code: 'ABORTED' })
    if (error instanceof TypeError) throw Object.assign(new Error('Connexion au serveur impossible. Réessayez lorsque le service est disponible.'), { code: 'NETWORK_ERROR' })
    throw error
  } finally {
    clearTimeout(timer)
    signal?.removeEventListener('abort', abort)
  }
}
export const getHealth = (signal) => request('/actuator/health', { signal })
export const reportsApi = {
  discard: (id, signal) => request(`/api/v2/signalements/${encodeURIComponent(id)}`, { method: 'DELETE', signal }),
  analyze: async (formData, signal) => assertReport(await request('/api/v2/signalements/analyser', {
    method: 'POST', body: formData, signal, timeoutMs: 35000,
  }), 'EN_ATTENTE'),
  confirm: async (id, corrections, signal) => {
    const report = assertReport(await request(`/api/v2/signalements/${encodeURIComponent(id)}/confirmer`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(corrections), signal,
    }), 'CONFIRME')
    if (report.id !== id) throw Object.assign(new Error('La confirmation concerne une autre fiche.'), { code: 'INVALID_RESPONSE' })
    return report
  },
  list: async (filters = {}, signal) => {
    const query = new URLSearchParams(Object.entries(filters).filter(([, value]) => value))
    return assertReportList(await request(`/api/v2/signalements${query.size ? `?${query}` : ''}`, { signal }))
  },
}

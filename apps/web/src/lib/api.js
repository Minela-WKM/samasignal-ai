// Same-origin /api: Vite proxies locally; nginx proxies in Docker.
export async function request(path, options = {}) {
  const response = await fetch(path, options)
  const body = await response.json().catch(() => null)
  if (!response.ok) {
    const error = new Error(body?.message || `Le service a répondu ${response.status}.`)
    error.status = response.status
    error.code = body?.code
    error.fieldErrors = body?.erreursChamps || {}
    throw error
  }
  return body
}

export const getHealth = (signal) => request('/actuator/health', { signal })

// Routes publiques du parcours citoyen.
export const reportsApi = {
  analyze: (formData, signal) => request('/api/v2/signalements/analyser', {
    method: 'POST', body: formData, signal,
  }),
  confirm: (id, corrections, signal) => request(`/api/v2/signalements/${encodeURIComponent(id)}/confirmer`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(corrections), signal,
  }),
  list: (filters = {}, signal) => {
    const query = new URLSearchParams(Object.entries(filters).filter(([, value]) => value))
    return request(`/api/v2/signalements${query.size ? `?${query}` : ''}`, { signal })
  },
}

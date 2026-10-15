// Same-origin /api: Vite proxies locally; nginx proxies in Docker.
export async function request(path, options = {}) {
  const response = await fetch(path, options)
  const body = await response.json().catch(() => null)
  if (!response.ok) {
    const error = new Error(body?.message || `Le service a répondu ${response.status}.`)
    error.status = response.status
    error.code = body?.code
    error.fieldErrors = body?.fieldErrors || {}
    throw error
  }
  return body
}

export const getHealth = (signal) => request('/actuator/health', { signal })

// Activate these routes once Dev B implements the contract. No fake fallback.
export const reportsApi = {
  analyze: (formData, signal) => request('/api/reports/analyze', {
    method: 'POST', body: formData, signal,
  }),
  confirm: (id, corrections, signal) => request(`/api/reports/${encodeURIComponent(id)}/confirm`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(corrections), signal,
  }),
  list: (filters = {}, signal) => {
    const query = new URLSearchParams(Object.entries(filters).filter(([, value]) => value))
    return request(`/api/reports${query.size ? `?${query}` : ''}`, { signal })
  },
}

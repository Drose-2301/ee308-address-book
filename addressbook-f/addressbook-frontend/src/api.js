const API_BASE = '/api'

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, options)
  if (!response.ok) {
    let message = `请求失败（HTTP ${response.status}）`
    try {
      const data = await response.json()
      if (data && data.message) message = data.message
    } catch {
      // ignore non-JSON error bodies
    }
    throw new Error(message)
  }
  if (response.status === 204) return null
  const contentType = response.headers.get('content-type') || ''
  return contentType.includes('application/json') ? response.json() : response
}

export function listContacts({ keyword = '', favorite = false } = {}) {
  const params = new URLSearchParams({ keyword, favorite: String(favorite) })
  return request(`/contacts?${params.toString()}`)
}

export function getStats() {
  return request('/contacts/stats')
}

export function createContact(payload) {
  return request('/contacts', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
}

export function updateContact(id, payload) {
  return request(`/contacts/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  })
}

export function deleteContact(id) {
  return request(`/contacts/${id}`, { method: 'DELETE' })
}

export function setFavorite(id, favorite) {
  return request(`/contacts/${id}/favorite`, {
    method: 'PATCH',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ favorite })
  })
}

export function importExcel(file) {
  const form = new FormData()
  form.append('file', file)
  return request('/contacts/import', { method: 'POST', body: form })
}

export async function exportExcel() {
  const response = await request('/contacts/export')
  const blob = await response.blob()
  const disposition = response.headers.get('content-disposition') || ''
  let fileName = '通讯录.xlsx'
  const utf8 = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8) {
    fileName = decodeURIComponent(utf8[1])
  }
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}

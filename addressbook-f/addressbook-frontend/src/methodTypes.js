/** Shared vocabulary for contact method types (kept in sync with the Java enum). */
export const METHOD_TYPES = [
  { value: 'PHONE', label: '电话', color: '#2563eb', bg: '#eff6ff' },
  { value: 'EMAIL', label: '邮箱', color: '#0d9488', bg: '#f0fdfa' },
  { value: 'WECHAT', label: '微信', color: '#16a34a', bg: '#f0fdf4' },
  { value: 'QQ', label: 'QQ', color: '#0284c7', bg: '#f0f9ff' },
  { value: 'OTHER', label: '其他', color: '#7c3aed', bg: '#f5f3ff' }
]

export function typeMeta(type) {
  return METHOD_TYPES.find(item => item.value === type) || METHOD_TYPES[4]
}

function createPlaceholder(label = 'C', background = '#dbeafe'): string {
  const text = label.slice(0, 1).toUpperCase()
  const svg = `
    <svg xmlns="http://www.w3.org/2000/svg" width="160" height="160" viewBox="0 0 160 160">
      <rect width="160" height="160" rx="28" fill="${background}" />
      <text x="50%" y="54%" dominant-baseline="middle" text-anchor="middle" font-size="64" font-family="Arial, sans-serif" fill="#1d4ed8">${text}</text>
    </svg>
  `
  return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`
}

export function resolveAssetUrl(
  value?: string,
  fallbackLabel = 'C',
  fallbackColor = '#dbeafe'
): string {
  if (!value) return createPlaceholder(fallbackLabel, fallbackColor)

  const normalized = value.trim()
  if (!normalized) return createPlaceholder(fallbackLabel, fallbackColor)

  if (/^https?:\/\//i.test(normalized) || normalized.startsWith('data:') || normalized.startsWith('blob:')) {
    return normalized
  }

  if (/[a-zA-Z]:\\/.test(normalized) || normalized.includes('\\')) {
    return createPlaceholder(fallbackLabel, fallbackColor)
  }

  const backendOrigin = import.meta.env.VITE_BACKEND_ORIGIN || 'http://localhost:8080'
  const path = normalized.startsWith('/') ? normalized : `/${normalized}`
  return `${backendOrigin}${path}`
}

export function resolveAvatarUrl(value?: string, label = 'U'): string {
  return resolveAssetUrl(value, label, '#e0f2fe')
}

export function resolveCoverUrl(value?: string, label = 'P'): string {
  return resolveAssetUrl(value, label, '#ede9fe')
}

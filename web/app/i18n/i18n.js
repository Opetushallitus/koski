import Cookie from 'js-cookie'

const texts = window.koskiLocalizationMap
const missing = {}

export const lang = Cookie.get('lang') || 'fi'

export const setLang = (newLang) => {
  Cookie.set('lang', newLang)
  window.location.reload()
}

export const t = (s, ignoreMissing, languageOverride) => {
  console.debug(`_localize: ${s}`)
  const usedLanguage = languageOverride || lang
  if (!s) return ''
  if (typeof s === 'object') {
    // assume it's a localized string
    return s[usedLanguage] || s.fi || s.sv || s.en
  }
  if (typeof s === 'string') {
    // try to find a localization from the bundle
    const localizedString = texts[s] || {}
    if (!localizedString[usedLanguage]) {
      if (ignoreMissing === true) return null
      if (!missing[usedLanguage + '.' + s]) {
        if (usedLanguage == 'fi')
          console.error(`Localization missing for language ${usedLanguage}:`, s)
        missing[usedLanguage + '.' + s] = true
      }
      return s
    }
    return localizedString[usedLanguage]
  }
  console.error('Trying to localize', s)
}

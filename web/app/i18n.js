import Cookie from 'js-cookie'

const texts = window.koskiLocalizationMap
const missing = {}

export const lang = Cookie.get('lang') || 'fi'

export const setLang = (newLang) => {
  Cookie.set('lang', newLang)
  window.location.reload()
}

console.log('Using language', lang)

export const t = (s, ignoreMissing, languageOverride) => {
  let usedLanguage = languageOverride || lang
  if (!s) return ''
  if (typeof s == 'object') {
    // assume it's a localized string
    return s[usedLanguage] || s['fi']
  }
  if (typeof s == 'string') {
    // try to find a localization from the bundle
    let localizedString = texts[s] || {}
    if (!localizedString[usedLanguage]) {
      if (ignoreMissing === true) return null
      if (!missing[usedLanguage + '.' + s]) {
        if (usedLanguage == 'fi') console.error(`Localization missing for language ${usedLanguage}:`, s)
        missing[usedLanguage + '.' + s] = true
      }
      return s
    }
    return localizedString[usedLanguage]
  }
  console.err('Trying to localize', s)
}
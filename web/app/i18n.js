import Cookie from 'js-cookie'

const texts = window.koskiLocalizationMap

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
    if (!texts[s]) {
      if (ignoreMissing === true) return null
      console.error('Localization missing:', s)
      texts[s] = { [usedLanguage]: s }
    }
    let localizedString = texts[s]
    if (!localizedString[usedLanguage]) {
      if (ignoreMissing === true) return null
      console[usedLanguage == 'fi' ? 'error' : 'log'](`Localization missing for language ${usedLanguage}:`, s)
      localizedString[usedLanguage] = localizedString.fi
    }
    return localizedString[usedLanguage]
  }
  console.err('Trying to localize', s)
}
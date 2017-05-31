import Cookie from 'js-cookie'

const texts = window.koskiLocalizationMap

export const lang = Cookie.get('lang') || 'fi'

export const setLang = (newLang) => {
  Cookie.set('lang', newLang)
  window.location.reload()
}

console.log('Using language', lang)
export const t = (s, ignoreMissing) => {
  if (!s) return ''
  if (typeof s == 'object') {
    // assume it's a localized string
    return s[lang] || s['fi']
  }
  if (typeof s == 'string') {
    // try to find a localization from the bundle
    if (!texts[s]) {
      if (ignoreMissing === true) return null
      console.error('Localization missing:', s)
      texts[s] = { [lang]: s }
    }
    let localizedString = texts[s]
    if (!localizedString[lang]) {
      if (ignoreMissing === true) return null
      console.error(`Localization missing for language ${lang}:`, s)
      localizedString[lang] = localizedString.fi
    }
    return localizedString[lang]
  }
  console.err('Trying to localize', s)
}
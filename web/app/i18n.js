import finnishTexts from './fi'
import {getCookie} from './cookie'

export const lang = getCookie('lang') || 'fi'
export const texts = lang == 'fi'  ? finnishTexts : {}
export const t = (localizedString) => {
  if (!localizedString) return ''
  if (typeof localizedString == 'object') {
    // assume it's a localized string
    return localizedString[lang] || localizedString['fi']
  }
  if (typeof localizedString == 'string') {
    // try to find a localization from the bundle
    if (!texts[localizedString]) {
      console.log('Localization missing:', localizedString)
      texts[localizedString] = localizedString
    }
    return texts[localizedString]
  }
  console.err('Trying to localize', localizedString)
}
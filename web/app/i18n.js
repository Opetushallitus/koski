import finnishTexts from './fi'
export const lang = 'fi'
export const texts = finnishTexts
export const t = (localizedString) => {
  if (!localizedString) return ''
  if (localizedString && localizedString[lang]) return localizedString[lang]
  if (!texts[localizedString]) {
    console.log('Localization missing:', localizedString)
    texts[localizedString] = localizedString
  }
  return texts[localizedString]
}
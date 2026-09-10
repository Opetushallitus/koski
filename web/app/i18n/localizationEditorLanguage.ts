/**
 * Lokalisointien muokkaajan kielen esikatselu on puhtaasti selaimen tilaa: palvelin ei tiedä siitä
 * mitään, eikä kieltä kirjoiteta evästeeseen, jota myös palvelin kirjoittaisi. sessionStorage
 * rajaa esikatselun yhteen välilehteen, joten se ei jää voimaan seuraavalle käyttäjälle.
 *
 * Ei importteja i18n:stä, jottei synny sykliä: i18n lukee esikatselukielen tästä.
 */
const storageKey = 'koskiLocalizationEditorLanguage'

export const getLocalizationEditorLanguage = (): string | undefined => {
  try {
    return sessionStorage.getItem(storageKey) ?? undefined
  } catch (err) {
    console.error('sessionStorage error', err)
    return undefined
  }
}

export const hasLocalizationEditorLanguage = (): boolean =>
  getLocalizationEditorLanguage() !== undefined

export const setLocalizationEditorLanguage = (language: string): void => {
  try {
    sessionStorage.setItem(storageKey, language)
  } catch (err) {
    console.error('sessionStorage error', err)
  }
  window.location.reload()
}

export const clearLocalizationEditorLanguage = (): boolean => {
  const hadOverride = hasLocalizationEditorLanguage()
  try {
    sessionStorage.removeItem(storageKey)
  } catch (err) {
    console.error('sessionStorage error', err)
  }
  return hadOverride
}

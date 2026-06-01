import { createPreferLocalCache } from '../api-fetch'
import { fetchVersiohistoria } from './koskiApi'

// Opiskeluoikeuden versiolista muuttuu vain tallennettaessa (uusi versio).
// Käytetään prefer-local-välimuistia, jotta versiosta toiseen siirtyminen
// (joka uudelleenkiinnittää editorin) ei hae listaa toistuvasti backendista,
// vaan lista tarjoillaan välimuistista — kuten vanha käyttöliittymä
// (Http.cachedGet). Tallennuksen jälkeen välimuisti tyhjennetään, jotta juuri
// luotu versio ilmestyy listaan seuraavalla avauksella.
export const versiohistoriaCache = createPreferLocalCache(fetchVersiohistoria)

export const invalidateVersiohistoriaCache = (): void =>
  versiohistoriaCache.clearAll()

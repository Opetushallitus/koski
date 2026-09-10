import Atom from 'bacon.atom'
import { lang } from './i18n'
import {
  hasLocalizationEditorLanguage,
  clearLocalizationEditorLanguage
} from './localizationEditorLanguage'
import http from '../util/http'
import { userP } from '../util/user'
import * as L from 'partial.lenses'
import * as R from 'ramda'

const changes = Atom({})
export const hasEditAccess = userP.map('.hasLocalizationWriteAccess')
// Ilman muokkausoikeutta ei jäädä muokkaustilaan eikä esikatselukieleen (esim. yhteiskäyttökone).
// Esikatselu poistetaan lataamatta sivua uudelleen: kieli korjautuu seuraavassa siirtymässä, eikä
// käyttäjän kesken olevaa sivua nykäistä alta.
hasEditAccess
  .not()
  .filter(R.identity)
  .onValue(() => {
    editAtom.set(false)
    clearLocalizationEditorLanguage()
  })
// Esikatselukieli tarkoittaa aina käynnissä olevaa muokkausta: se säilyy sivunlatauksen yli.
export const editAtom = Atom(hasLocalizationEditorLanguage())
export const startEdit = () => {
  editAtom.set(true)
}
export const hasChanges = changes.map((c) => R.keys(c).length > 0)
export const saveChanges = () => {
  const changeList = R.toPairs(changes.get()).map(([langAndKey, value]) => ({
    key: langAndKey.substring(3),
    value,
    locale: langAndKey.substring(0, 2)
  }))
  // Paikallinen käännöskartta päivitetään vasta onnistuneen tallennuksen jälkeen,
  // jottei epäonnistunut tallennus jätä näkyviin tekstejä, joita ei ole tallennettu.
  // Muokkaustila ja valittu kieli säilyvät, jotta käännöstyötä voi jatkaa samalla
  // kielellä; vain Peruuta ja uloskirjautuminen päättävät muokkauksen.
  http.put('/koski/api/localization', changeList).onValue(() => {
    changeList.forEach(({ key, value, locale }) => {
      window.koskiLocalizationMap[key] = {
        ...window.koskiLocalizationMap[key],
        [locale]: value
      }
    })
    changes.set({})
  })
}
export const cancelChanges = () => {
  changes.set({})
  editAtom.set(false)
  if (clearLocalizationEditorLanguage()) {
    window.location.reload()
  }
}
export const changeText = (key, value, language) =>
  changes.modify((cs) => L.set([(language || lang) + '.' + key], value, cs))

export const languages = ['fi', 'sv', 'en']

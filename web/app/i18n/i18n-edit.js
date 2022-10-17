import Atom from 'bacon.atom'
import { lang } from './i18n'
import http from '../util/http'
import { userP } from '../util/user'
import * as L from 'partial.lenses'
import * as R from 'ramda'
import { parseBool } from '../util/util'

const changes = Atom({})
export const hasEditAccess = userP.map('.hasLocalizationWriteAccess')
hasEditAccess
  .not()
  .filter(R.identity)
  .onValue(() => editAtom.set(false))
export const editAtom = Atom(parseBool(localStorage.editLocalizations))
try {
  localStorage.editLocalizations = false
} catch (err) {
  console.error('localStorage error', err)
}
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
  changeList.forEach(({ key, value, locale }) => {
    window.koskiLocalizationMap[key][locale] = value
  })
  http.put('/koski/api/localization', changeList)
  cancelChanges()
}
export const cancelChanges = () => {
  changes.set({})
  editAtom.set(false)
}
export const changeText = (key, value, language) =>
  changes.modify((cs) => L.set([(language || lang) + '.' + key], value, cs))

export const languages = ['fi', 'sv', 'en']

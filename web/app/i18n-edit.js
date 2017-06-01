import Atom from 'bacon.atom'
import {lang} from './i18n'
import http from './http'
import {userP} from './user'
import * as L from 'partial.lenses'
import R from 'ramda'
import {parseBool} from './util'

let changes = Atom({})
export const hasEditAccess = userP.map('.hasLocalizationWriteAccess')
hasEditAccess.not().filter(R.identity).onValue(() => editAtom.set(false))
export const editAtom = Atom(parseBool(localStorage.editLocalizations))
localStorage.editLocalizations = false
export const startEdit = () => {
  editAtom.set(true)
}
export const hasChanges = changes.map(c => R.keys(c).length > 0)
export const saveChanges = () => {
  let changeList = R.toPairs(changes.get()).map(([langAndKey, value]) => ({ key: langAndKey.substring(3), value, locale: langAndKey.substring(0, 2)}))
  changeList.forEach(({key, value, locale}) => {
    window.koskiLocalizationMap[key][locale] = value
  })
  http.put('/koski/api/localization', changeList)
  cancelChanges()
}
export const cancelChanges = () => {
  changes.set({})
  editAtom.set(false)
}
export const changeText = (key, value, language) => changes.modify(cs => L.set([(language || lang) + '.' + key], value, cs))

export const languages = ['fi', 'sv', 'en']

editAtom.changes().onValue(e => localStorage.editLocalizations = e)
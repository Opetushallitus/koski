import Atom from 'bacon.atom'
import { lang } from './i18n'
import http from './http'
import { userP } from './user'
import * as L from 'partial.lenses'
import R from 'ramda'

let changes = Atom({})
export const hasEditAccess = userP.map('.hasLocalizationWriteAccess')
export const edit = Atom(false)
export const startEdit = () => {
  edit.set(true)
}
export const hasChanges = changes.map(c => R.keys(c).length > 0)
export const saveChanges = () => {
  let changeList = R.toPairs(changes.get()).map(([key, value]) => ({ key, value, locale: lang}))
  changeList.forEach(({key, value, locale}) => {
    window.koskiLocalizationMap[key][locale] = value
  })
  http.put('/koski/api/localization', changeList)
  edit.set(false)
}
export const cancelChanges = () => {
  changes.set({})
  edit.set(false)
}
export const changeText = (key, value) => changes.modify(cs => L.set([key], value, cs))
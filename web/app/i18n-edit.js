import Atom from 'bacon.atom'
import { lang } from './i18n'
import http from './http'

let changes = Atom([])
export const edit = Atom(false)
export const startEdit = () => {
  edit.set(true)
}
export const hasChanges = changes.map(c => c.length > 0)
export const saveChanges = () => {
  changes.get().forEach(({key, value}) => {
    window.koskiLocalizationMap[key][lang] = value
  })
  http.put('/koski/api/localization', changes.get())
  edit.set(false)
}
export const cancelChanges = () => {
  changes.set([])
  edit.set(false)
}
export const changeText = (key, value) => changes.modify(c => c.concat({key, value, locale: lang}))
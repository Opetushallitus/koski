import Atom from 'bacon.atom'
import { lang } from './i18n'

let changes = Atom([])
export const edit = Atom(false)
export const startEdit = () => {
  edit.set(true)
}
export const hasChanges = changes.map(c => c.length > 0)
export const saveChanges = () => {
  changes.get().forEach(({key, text}) => {
    window.koskiLocalizationMap[key][lang] = text
  })
  // TODO: actually save changes here
  edit.set(false)
}
export const cancelChanges = () => {
  changes.set([])
  edit.set(false)
}
export const changeText = (key, text) => changes.modify(c => c.concat({key, text}))
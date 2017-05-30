import Atom from 'bacon.atom'

let changes = Atom([])
export const edit = Atom(false)
export const startEdit = () => {
  edit.set(true)
}
export const hasChanges = changes.map(c => c.length > 0)
export const saveChanges = () => {
  // TODO: actually save changes here
}
export const cancelChanges = () => {
  changes.set([])
  edit.set(false)
}
export const changeText = (key, text) => changes.modify(c => c.concat({key, text}))
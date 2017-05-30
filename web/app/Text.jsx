import React from 'baret'
import { startEdit, edit, changeText } from './i18n-edit'
import { t } from './i18n'

export default ({name, ignoreMissing}) => {
  if (typeof name != 'string') {
    console.error('Not a string', name)
    return <span>{'NOT A STRING'}</span>
  }

  let onClick = e => {
    let isEdit = edit.get()
    if (e.getModifierState('Alt') && !isEdit) {
      startEdit()
      e.stopPropagation()
      e.preventDefault()
    } else if (isEdit) {
      e.stopPropagation()
      e.preventDefault()
    }
  }
  let onChange = (event) => changeText(name, event.target.value)
  let current = () => t(name, ignoreMissing)

  return (<span onClick={onClick} className="localized">{
    edit.map(isEdit => isEdit ? <span className="editing"><input placeholder={current()} onChange={onChange}/></span> : current())
  }</span>)
}
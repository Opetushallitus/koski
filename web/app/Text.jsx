import React from 'baret'
import {edit, changeText} from './i18n-edit'
import {t} from './i18n'

export default ({name, ignoreMissing}) => {
  if (typeof name != 'string') {
    console.error('Not a string', name)
    return <span>{'NOT A STRING'}</span>
  }

  let onClick = e => {
    if (edit.get()) {
      e.stopPropagation()
      e.preventDefault()
    }
  }
  let onChange = (event) => changeText(name, event.target.value)
  let current = () => t(name, ignoreMissing)

  return (<span onClick={onClick} className="localized">{
    edit.map(isEdit => isEdit ? <span className="editing"><input defaultValue={current()} onChange={onChange}/></span> : current())
  }</span>)
}
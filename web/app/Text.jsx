import React from 'baret'
import {editAtom, changeText} from './i18n-edit'
import {t} from './i18n'
import Bacon from 'baconjs'

export default ({name, ignoreMissing, lang, edit}) => {
  let editP = edit == undefined ? editAtom : Bacon.constant(edit)

  if (typeof name != 'string') {
    console.error('Not a string', name)
    return <span>{'NOT A STRING'}</span>
  }

  let onClick = e => {
    e.stopPropagation()
    e.preventDefault()
  }

  let onChange = (event) => changeText(name, event.target.value, lang)
  let current = () => t(name, ignoreMissing, lang)

  return (<span onClick={editP.map(e => e ? onClick : null)} className="localized">{
    editP.map(e => e ? <span className="editing"><input defaultValue={current()} onChange={onChange}/></span> : current())
  }</span>)
}
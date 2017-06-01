import React from 'baret'
import {editAtom, changeText} from './i18n-edit'
import {t} from './i18n'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'

export default ({name, ignoreMissing, lang, edit}) => {
  let editP = edit == undefined ? editAtom : Bacon.constant(edit)

  if (typeof name != 'string') {
    console.error('Not a string', name)
    return <span>{'NOT A STRING'}</span>
  }

  return (<span className="localized">{
    editP.map(e => e
      ? <TextEditor {...{name, lang}}/>
      : t(name, ignoreMissing, lang))
  }</span>)
}

const TextEditor = ({name, lang}) => {
  let changed = Atom(false)
  let onClick = e => {
    e.stopPropagation()
    e.preventDefault()
  }

  let onInput = (event) => {
    changed.set(true)
    changeText(name, event.target.textContent, lang)
  }
  let wasMissing = !t(name, true, lang)
  let missingP = changed.map(c => !c && wasMissing)
  let classNameP = missingP.map(missing => 'editing' + (missing ? ' missing': ''))
  let editableValue = t(name, false, lang)

  return (<span className={classNameP} contentEditable="true" suppressContentEditableWarning="true" onKeyUp={onInput} onClick={onClick}>{editableValue}</span>)
}
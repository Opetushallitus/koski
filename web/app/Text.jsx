import React from 'baret'
import {editAtom, changeText} from './i18n-edit'
import {parseBool} from './util'
import {t} from './i18n'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'

export default ({name, ignoreMissing, lang, edit, labelFor}) => {
  let editP = edit == undefined ? editAtom : Bacon.constant(parseBool(edit))

  if (typeof name != 'string') {
    console.error('Not a string', name)
    return <span>{'NOT A STRING'}</span>
  }

  if (labelFor !== undefined && document.getElementById(labelFor)) {
    return (
      <label htmlFor={labelFor}>
        <span className="localized">
          {t(name, ignoreMissing, lang)}
        </span>
      </label>
    )
  }

  return (<span className="localized">{
    editP.map(e => e
      ? <TextEditor {...{name, lang}}/>
      : t(name, ignoreMissing, lang))
  }</span>)
}

const TextEditor = ({name, lang}) => {
  let currentValue = t(name, false, lang)
  let changed = Atom(false)
  let onClick = e => {
    e.stopPropagation()
    e.preventDefault()
  }

  let onInput = (event) => {
    var newValue = event.target.textContent
    if (newValue != currentValue) {
      currentValue = newValue
      changed.set(true)
      changeText(name, newValue, lang)
    }
  }
  let wasMissing = !t(name, true, lang)
  let missingP = changed.map(c => !c && wasMissing)
  let classNameP = missingP.map(missing => 'editing' + (missing ? ' missing': ''))

  return (<span className={classNameP} contentEditable="true" suppressContentEditableWarning="true" onKeyUp={onInput} onInput={onInput} onClick={onClick}>{currentValue}</span>)
}

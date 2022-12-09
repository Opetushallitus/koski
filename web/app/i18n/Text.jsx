import React from 'baret'
import { editAtom, changeText } from './i18n-edit'
import { parseBool } from '../util/util'
import { t } from './i18n'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import { buildClassNames } from '../components/classnames'

export default ({ name, ignoreMissing, lang, edit, className, ...rest }) => {
  const editP = edit === undefined ? editAtom : Bacon.constant(parseBool(edit))

  if (name === null || name === undefined) {
    return null
  }

  if (typeof name !== 'string') {
    console.error('Not a string', name)
    return <span>{'NOT A STRING'}</span>
  }

  return (
    <span
      aria-label={t(name, ignoreMissing, lang)}
      className={buildClassNames([className, 'localized'])}
      {...rest}
    >
      {editP.map((e) =>
        e ? <TextEditor {...{ name, lang }} /> : t(name, ignoreMissing, lang)
      )}
    </span>
  )
}

const TextEditor = ({ name, lang }) => {
  let currentValue = t(name, false, lang)
  const changed = Atom(false)
  const onClick = (e) => {
    e.stopPropagation()
    e.preventDefault()
  }

  const onInput = (event) => {
    const newValue = event.target.textContent
    if (newValue !== currentValue) {
      currentValue = newValue
      changed.set(true)
      changeText(name, newValue, lang)
    }
  }
  const wasMissing = !t(name, true, lang)
  const missingP = changed.map((c) => !c && wasMissing)
  const classNameP = missingP.map(
    (missing) => 'editing' + (missing ? ' missing' : '')
  )

  return (
    <span
      className={classNameP}
      contentEditable="true"
      suppressContentEditableWarning="true"
      onKeyUp={onInput}
      onInput={onInput}
      onClick={onClick}
      aria-label={currentValue}
    >
      {currentValue}
    </span>
  )
}

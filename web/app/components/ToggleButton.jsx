import React from 'baret'
import Text from '../i18n/Text'

const toggleState = (stateA) => () => stateA.modify((v) => !v)
const setOrClearState = (stateA, value, clearedStateValue) => () =>
  stateA.modify((current) => (value === current ? clearedStateValue : value))

export const ToggleButton = ({ toggleA, text, style }) =>
  style === 'secondary' ? (
    <button
      className="koski-button toggle-button koski-button-secondary"
      onClick={toggleState(toggleA)}
    >
      <Text name={text} />
    </button>
  ) : (
    <button
      className="koski-button toggle-button"
      onClick={toggleState(toggleA)}
    >
      <Text name={text} />
    </button>
  )

export const MultistateToggleButton = ({
  id,
  stateA,
  value,
  clearedStateValue = null,
  text,
  style
}) =>
  style === 'text' ? (
    <button
      id={id}
      className="koski-button toggle-button koski-button-secondary"
      aria-pressed={stateA.map((mode) => mode === value)}
      onClick={setOrClearState(stateA, value, clearedStateValue)}
    >
      <Text name={text} />
    </button>
  ) : (
    <button
      id={id}
      className="koski-button toggle-button"
      aria-pressed={stateA.map((mode) => mode === value)}
      onClick={setOrClearState(stateA, value, clearedStateValue)}
    >
      <Text name={text} />
    </button>
  )

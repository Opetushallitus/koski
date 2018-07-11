import React from 'baret'
import Text from '../i18n/Text'

const toggleState = stateA => () => stateA.modify(v => !v)
const setOrClearState = (stateA, value, clearedStateValue) => () =>
  stateA.modify(current => value === current ? clearedStateValue : value)

export const ToggleButton = ({toggleA, text, style}) => style === 'text'
  ? <button className='toggle-button text-button' onClick={toggleState(toggleA)}><Text name={text}/></button>
  : <button className='toggle-button' onClick={toggleState(toggleA)}><Text name={text}/></button>

export const MultistateToggleButton = ({stateA, value, clearedStateValue = null, text, style}) => style === 'text'
  ? (
    <button className='toggle-button text-button' aria-pressed={stateA.map(mode => mode === value)} onClick={setOrClearState(stateA, value, clearedStateValue)}>
      <Text name={text}/>
    </button>
  ) : (
    <button className='koski-button toggle-button' aria-pressed={stateA.map(mode => mode === value)} onClick={setOrClearState(stateA, value, clearedStateValue)}>
      <Text name={text}/>
    </button>
  )

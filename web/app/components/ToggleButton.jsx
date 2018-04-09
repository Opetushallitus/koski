import React from 'react'
import Text from '../i18n/Text'

const toggleState = stateA => () => stateA.modify(v => !v)

export const ToggleButton = ({toggleA, text, style}) => style === 'text'
  ? <a className='toggle-button text-button' onClick={toggleState(toggleA)}><Text name={text}/></a>
  : <button className='toggle-button' onClick={toggleState(toggleA)}><Text name={text}/></button>

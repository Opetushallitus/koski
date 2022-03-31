import React from 'baret'

const Input = ({id, type, disabled, value, autofocus = false}) => (
  <input
    type={type}
    disabled={disabled}
    value={value.or('')}
    onChange={e => value.set(e.target.value)}
    id={id}
    autoFocus={autofocus}>
  </input>
)

Input.displayName = 'Input'

export default Input

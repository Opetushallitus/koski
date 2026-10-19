import React from '../util/baret'

const Input = ({ id, type, disabled, value, autofocus = false }) => (
  <input
    type={type}
    disabled={disabled}
    value={value.or('')}
    onChange={(e) => value.set(e.target.value)}
    id={id}
    autoFocus={autofocus}
    data-testid={id}
  ></input>
)

export default Input

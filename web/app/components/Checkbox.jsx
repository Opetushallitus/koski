import React from 'react'
import Text from '../i18n/Text'

const Checkbox = ({
  id,
  onChange,
  checked,
  label,
  LabelComponent,
  listStylePosition,
  disabled
}) => (
  <div className="koski-checkbox">
    <input
      className="koski-checkbox__input"
      type="checkbox"
      id={id}
      data-testid={id}
      checked={checked}
      onChange={onChange}
      data-list-style-position={listStylePosition === 'inside' ? 'inside' : ''}
      disabled={disabled}
    />

    <label htmlFor={id} data-testid={id + '__label'}>
      <span className="koski-checkbox__checkmark" aria-hidden={true} />
      {LabelComponent ? <LabelComponent /> : <Text name={label} />}
    </label>
  </div>
)

export default Checkbox

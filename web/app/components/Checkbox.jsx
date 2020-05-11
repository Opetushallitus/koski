import React from 'react'
import Text from '../i18n/Text'

const Checkbox = ({id, onChange, checked, label, LabelComponent, listStylePosition}) => (
  <div className='koski-checkbox'>
    <input
      className='koski-checkbox__input'
      type='checkbox'
      id={id}
      checked={checked}
      onChange={onChange}
      data-list-style-position={listStylePosition === 'inside' ? 'inside' : ''}
    />

    <label htmlFor={id}>
      <span className='koski-checkbox__checkmark' aria-hidden={true}/>
      {
        LabelComponent && <LabelComponent/>
      }
      {
        label && <Text name={label}/>
      }
    </label>
  </div>
)

export default Checkbox

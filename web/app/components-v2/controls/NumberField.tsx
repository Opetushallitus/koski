import React, { useCallback } from 'react'
import { t } from '../../i18n/i18n'
import { baseProps, BaseProps } from '../baseProps'
import { FieldErrors } from '../forms/FieldErrors'

export type NumberFieldProps = BaseProps & {
  value?: number
  onChange: (text: number) => void
}

export const NumberField: React.FC<NumberFieldProps> = (props) => {
  const onChange: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      props.onChange(parseFloat(event.target.value))
    },
    [props.onChange]
  )
  return (
    <div {...baseProps(props, 'NumberField')}>
      <input
        className="NumberField__input"
        type="number"
        value={props.value}
        onChange={onChange}
      />
    </div>
  )
}

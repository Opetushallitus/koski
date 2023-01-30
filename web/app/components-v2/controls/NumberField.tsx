import React, { useCallback } from 'react'
import { CommonProps, cx, common } from '../CommonProps'

export type NumberFieldProps = CommonProps<{
  value?: number
  onChange: (text: number) => void
  hasErrors?: boolean
}>

export const NumberField: React.FC<NumberFieldProps> = (props) => {
  const { onChange } = props
  const onChangeCB: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      onChange(parseFloat(event.target.value))
    },
    [onChange]
  )
  return (
    <div {...common(props, ['NumberField'])}>
      <input
        className={cx(
          'NumberField__input',
          props.hasErrors && 'NumberField__input--error'
        )}
        type="number"
        value={props.value}
        onChange={onChangeCB}
      />
    </div>
  )
}

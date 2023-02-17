import React, { useCallback, useEffect, useState } from 'react'
import { CommonProps, cx, common, testId } from '../CommonProps'

export type NumberFieldProps = CommonProps<{
  value?: number
  onChange: (text: number) => void
  hasErrors?: boolean
}>

export const NumberField: React.FC<NumberFieldProps> = (props) => {
  const [internalValue, setInternalValue] = useState(props.value)
  useEffect(() => setInternalValue(props.value), [props.value])

  const { onChange } = props
  const onChangeCB: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      const value = parseFloat(event.target.value)
      setInternalValue(value)
      onChange(value)
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
        value={internalValue}
        onChange={onChangeCB}
        {...testId(props, 'input')}
      />
    </div>
  )
}

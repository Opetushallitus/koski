import React, { useCallback, useEffect, useState } from 'react'
import { CommonProps, cx, common, testId } from '../CommonProps'

export type NumberFieldProps = CommonProps<{
  value?: number
  onChange: (text: number) => void
  hasErrors?: boolean
}>

export const NumberField: React.FC<NumberFieldProps> = (props) => {
  const [internalValue, setInternalValue] = useState(props.value?.toString())
  useEffect(() => setInternalValue(props.value?.toString()), [props.value])

  const { onChange } = props
  const onChangeCB: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      setInternalValue(event.target.value)
      const value = parseFloat(event.target.value)
      if (Number.isFinite(value)) {
        onChange(value)
      }
    },
    [onChange]
  )

  const onBlurCB = useCallback(() => {
    const value = parseFloat(internalValue || '')
    onChange(Number.isFinite(value) ? value : 0)
  }, [internalValue, onChange])

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
        onBlur={onBlurCB}
        {...testId(props, 'input')}
      />
    </div>
  )
}

import React, { useCallback } from 'react'
import { common, CommonProps, cx } from '../CommonProps'
import { useTestId } from '../../appstate/useTestId'

export type NumberFieldProps = CommonProps<{
  value?: number
  onChange: (text?: number) => void
  hasErrors?: boolean
  testId?: string | number
}>

export const NumberField: React.FC<NumberFieldProps> = (props) => {
  const testId = useTestId(props.testId, 'input')

  const { onChange, value } = props
  const onChangeCB: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      if (event.target.value === '') {
        onChange(undefined)
      }
      const number = parseFloat(event.target.value)
      if (Number.isFinite(number)) {
        onChange(number)
      }
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
        value={value ? value.toString() : ''}
        onChange={onChangeCB}
        data-testid={testId}
      />
    </div>
  )
}

import React, { useMemo } from 'react'
import { t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { common, CommonProps } from '../CommonProps'
import { useTestId } from '../../appstate/useTestId'

export type CheckboxProps = CommonProps<{
  label: string | LocalizedString
  checked: boolean
  onChange: (checked: boolean) => void
  testId: string
}>

export const Checkbox: React.FC<CheckboxProps> = (props) => {
  const id = useMemo(() => Math.random().toString(), [])
  const testId = useTestId(props.testId)
  return (
    <div {...common(props, ['Checkbox'])} data-testid={testId}>
      <input
        id={id}
        key={Math.random()}
        className="Checkbox__input"
        type="checkbox"
        checked={props.checked}
        onChange={(_) => {}}
        data-testid={`${testId}.input`}
      />
      <label
        htmlFor={id}
        onClick={() => {
          console.log('label onClick')
          return props.onChange(!props.checked)
        }}
        data-testid={`${testId}.label`}
      >
        <span className="Checkbox__label">{t(props.label)}</span>
      </label>
    </div>
  )
}

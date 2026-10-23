import React from 'react'
import { t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { common, CommonProps } from '../CommonProps'
import { useTestId } from '../../appstate/useTestId'
import { useUniqueId } from '../../util/useUniqueId'

export type CheckboxProps = CommonProps<{
  label: string | LocalizedString
  checked: boolean
  onChange: (checked: boolean) => void
  testId: string
}>

export const Checkbox: React.FC<CheckboxProps> = (props) => {
  const id = useUniqueId('Checkbox')
  const testId = useTestId(props.testId)
  return (
    <div {...common(props, ['Checkbox'])} data-testid={testId}>
      <input
        id={id}
        className="Checkbox__input"
        type="checkbox"
        checked={props.checked}
        onChange={(_) => {
          props.onChange(!props.checked)
        }}
        data-testid={`${testId}.input`}
      />
      <label htmlFor={id} data-testid={`${testId}.label`}>
        <span className="Checkbox__label">{t(props.label)}</span>
      </label>
    </div>
  )
}

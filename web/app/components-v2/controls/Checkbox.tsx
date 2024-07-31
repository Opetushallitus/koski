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
    <div {...common(props, ['Checkbox'])}>
      <input
        id={id}
        key={Math.random()}
        className="Checkbox__input"
        type="checkbox"
        checked={props.checked}
        onChange={() => props.onChange(!props.checked)}
        data-testid={testId}
      />
      <label htmlFor={id} onClick={() => props.onChange(!props.checked)}>
        <span className="Checkbox__label">{t(props.label)}</span>
      </label>
    </div>
  )
}

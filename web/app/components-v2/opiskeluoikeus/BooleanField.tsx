import React from 'react'
import { TestIdLayer, TestIdText } from '../../appstate/useTestId'
import { CommonProps } from '../CommonProps'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { Checkbox } from '../controls/Checkbox'
import { t } from '../../i18n/i18n'

export type BooleanViewProps = CommonProps<
  FieldViewerProps<
    boolean | undefined,
    {
      hideFalse?: boolean
      trueText?: string
      falseText?: string
    }
  >
>

export const BooleanView: React.FC<BooleanViewProps> = ({
  children,
  trueText,
  falseText,
  testId,
  value,
  hideFalse,
  ...rest
}) => {
  if (hideFalse && value === false) {
    return null
  }
  return value === undefined ? null : (
    <TestIdText id={testId} {...rest}>
      {value ? trueText || t('Kyllä') : falseText || t('Ei')}
    </TestIdText>
  )
}

export type BooleanEditProps = CommonProps<
  FieldEditorProps<
    boolean,
    {
      label?: string
    }
  >
>

export const BooleanEdit: React.FC<BooleanEditProps> = ({
  testId,
  label,
  value,
  onChange
}) => (
  <Checkbox
    label={label || t('Kyllä')}
    checked={!!value}
    onChange={onChange}
    testId={`${testId || 'bool'}`}
  />
)

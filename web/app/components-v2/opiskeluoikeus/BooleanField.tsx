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
  ...rest
}) =>
  value === undefined ? null : (
    <TestIdText id={testId} {...rest}>
      {value ? trueText || t('Kyllä') : falseText || t('Ei')}
    </TestIdText>
  )

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
    onChange={(ch) => (console.log('checked', value, '-->', ch), onChange(ch))}
    testId={`${testId || 'bool'}`}
  />
)

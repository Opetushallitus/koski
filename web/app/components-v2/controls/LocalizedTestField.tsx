import React from 'react'
import { t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { common, CommonProps } from '../CommonProps'
import { FieldViewerProps } from '../forms/FormField'
import { TestIdText } from '../../appstate/useTestId'

export type LocalizedTextViewProps = CommonProps<
  FieldViewerProps<LocalizedString, {}>
>

export const LocalizedTextView: React.FC<LocalizedTextViewProps> = (props) => (
  <TestIdText {...common(props)} id={props.testId}>
    {t(props.value)}
  </TestIdText>
)

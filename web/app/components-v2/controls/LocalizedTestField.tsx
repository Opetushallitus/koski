import React from 'react'
import { TestIdText } from '../../appstate/useTestId'
import { t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { EmptyObject } from '../../util/objects'
import { common, CommonProps } from '../CommonProps'
import { FieldViewerProps } from '../forms/FormField'

export type LocalizedTextViewProps = CommonProps<
  FieldViewerProps<LocalizedString, EmptyObject>
>

export const LocalizedTextView: React.FC<LocalizedTextViewProps> = (props) => (
  <TestIdText {...common(props)} id={props.testId}>
    {t(props.value)}
  </TestIdText>
)

import React from 'react'
import { t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { common, CommonProps, testId } from '../CommonProps'
import { FieldViewerProps } from '../forms/FormField'

export type LocalizedTextViewProps = CommonProps<
  FieldViewerProps<LocalizedString, {}>
>

export const LocalizedTextView: React.FC<LocalizedTextViewProps> = (props) => (
  <span {...common(props)} {...testId(props)}>
    {t(props.value)}
  </span>
)

import React from 'react'
import { TestIdText } from '../../appstate/useTestId'
import { localize, t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { EmptyObject } from '../../util/objects'
import { common, CommonProps } from '../CommonProps'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { TextEdit } from './TextField'

export type LocalizedTextViewProps = CommonProps<
  FieldViewerProps<LocalizedString, EmptyObject>
>

export const LocalizedTextView: React.FC<LocalizedTextViewProps> = (props) => (
  <TestIdText {...common(props)} id={props.testId}>
    {t(props.value)}
  </TestIdText>
)

export type LocalizedTextEditProps = CommonProps<
  FieldEditorProps<LocalizedString, EmptyObject>
> & {
  large?: boolean
}

export const LocalizedTextEdit: React.FC<LocalizedTextEditProps> = (props) => (
  <TestIdText {...common(props)} id={props.testId}>
    <TextEdit
      value={t(props.value)}
      large={props.large}
      onChange={(text) =>
        props.onChange(text === undefined ? undefined : localize(text))
      }
    />
  </TestIdText>
)

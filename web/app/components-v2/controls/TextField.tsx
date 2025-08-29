import React, { useCallback } from 'react'
import { TestIdText, useTestId } from '../../appstate/useTestId'
import { EmptyObject } from '../../types/EditorModels'
import { common, CommonProps, cx } from '../CommonProps'
import { FieldErrors } from '../forms/FieldErrors'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'

export type TextViewProps = CommonProps<FieldViewerProps<string, EmptyObject>>

export const TextView: React.FC<TextViewProps> = (props) =>
  props.value ? (
    <TestIdText {...common(props, ['TextView'])} id={props.testId}>
      {props.value}
    </TestIdText>
  ) : null

export type TextEditProps = CommonProps<
  FieldEditorProps<
    string,
    {
      placeholder?: string
      autoFocus?: boolean
      testId?: string
      disabled?: boolean
      large?: boolean
    }
  >
>

export const TextEdit: React.FC<TextEditProps> = (props) => {
  const testId = useTestId(props.testId, 'input')

  const { onChange, value } = props
  const onChangeCB: React.ChangeEventHandler<
    HTMLInputElement | HTMLTextAreaElement
  > = useCallback(
    (event) => {
      onChange(event.target.value === '' ? undefined : event.target.value)
    },
    [onChange]
  )

  return (
    <label {...common(props, ['TextEdit'])}>
      {props.large ? (
        <textarea
          className={cx(
            'TextEdit__input',
            props.errors && 'TextEdit__input--error'
          )}
          placeholder={props.placeholder}
          onChange={onChangeCB}
          autoFocus={props.autoFocus}
          disabled={props.disabled}
          data-testid={testId}
        >
          {value || ''}
        </textarea>
      ) : (
        <input
          className={cx(
            'TextEdit__input',
            props.errors && 'TextEdit__input--error'
          )}
          placeholder={props.placeholder}
          value={value || ''}
          onChange={onChangeCB}
          autoFocus={props.autoFocus}
          disabled={props.disabled}
          data-testid={testId}
        />
      )}
      <FieldErrors errors={props.errors} />
    </label>
  )
}

export const MultilineTextEdit: React.FC<TextEditProps> = (props) => {
  const testId = useTestId(props.testId, 'input')

  const { onChange } = props
  const onChangeCB: React.ChangeEventHandler<HTMLTextAreaElement> = useCallback(
    (event) => {
      onChange(event.target.value)
    },
    [onChange]
  )

  return (
    <label {...common(props, ['TextEdit'])}>
      <textarea
        className={cx(
          'TextEdit__input',
          props.errors && 'TextEdit__input--error'
        )}
        placeholder={props.placeholder}
        value={props.value}
        onChange={onChangeCB}
        autoFocus={props.autoFocus}
        data-testid={testId}
      />
      <FieldErrors errors={props.errors} />
    </label>
  )
}

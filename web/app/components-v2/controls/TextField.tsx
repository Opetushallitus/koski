import React, { useEffect, useCallback, useState } from 'react'
import { common, CommonProps, cx } from '../CommonProps'
import { FieldErrors } from '../forms/FieldErrors'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { useTestId } from '../../appstate/useTestId'

// eslint-disable-next-line @typescript-eslint/ban-types
export type TextViewProps = CommonProps<FieldViewerProps<string, {}>>

export const TextView: React.FC<TextViewProps> = (props) => (
  <div {...common(props, ['TextView'])}>{props.value}</div>
)

export type TextEditProps = CommonProps<
  FieldEditorProps<
    string,
    {
      placeholder?: string
      autoFocus?: boolean
      testId?: string
      disabled?: boolean
    }
  >
>

export const TextEdit: React.FC<TextEditProps> = (props) => {
  const testId = useTestId(props.testId, 'input')

  const [internalValue, setInternalValue] = useState(props.value)
  useEffect(() => setInternalValue(props.value), [props.value])

  const { onChange } = props
  const onChangeCB: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      setInternalValue(event.target.value)
      onChange(event.target.value)
    },
    [onChange]
  )

  return (
    <label {...common(props, ['TextEdit'])}>
      <input
        className={cx(
          'TextEdit__input',
          props.errors && 'TextEdit__input--error'
        )}
        placeholder={props.placeholder}
        value={internalValue}
        onChange={onChangeCB}
        autoFocus={props.autoFocus}
        disabled={props.disabled}
        data-testid={testId}
      />
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

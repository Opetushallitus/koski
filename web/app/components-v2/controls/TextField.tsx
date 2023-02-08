import * as A from 'fp-ts/Array'
import React, { useCallback } from 'react'
import { t } from '../../i18n/i18n'
import { common, CommonProps, cx } from '../CommonProps'
import { FieldErrors } from '../forms/FieldErrors'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { emptyString } from '../forms/validator'

export type TextViewProps = CommonProps<FieldViewerProps<string>>

export const TextView: React.FC<TextViewProps> = (props) => (
  <div {...common(props, ['TextView'])}>{props.value}</div>
)

export type TextEditProps = CommonProps<
  FieldEditorProps<string> & {
    placeholder?: string
    autoFocus?: boolean
    allowEmpty?: boolean
  }
>

export const TextEdit: React.FC<TextEditProps> = (props) => {
  const { onChange } = props
  const onChangeCB: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      onChange(event.target.value)
    },
    [onChange]
  )

  const requiredButEmpty = Boolean(!props.allowEmpty && !props.value)

  return (
    <label {...common(props, ['TextEdit'])}>
      <input
        className={cx(
          'TextEdit__input',
          (requiredButEmpty || props.errors) && 'TextEdit__input--error'
        )}
        placeholder={props.placeholder}
        value={props.value}
        onChange={onChangeCB}
        autoFocus={props.autoFocus}
      />
      <FieldErrors
        errors={props.errors}
        localErrors={requiredButEmpty ? [emptyString([])] : undefined}
      />
    </label>
  )
}

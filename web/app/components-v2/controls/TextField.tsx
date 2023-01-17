import React, { useCallback } from 'react'
import { common, CommonProps } from '../CommonProps'
import { FieldErrors } from '../forms/FieldErrors'
import { FieldEditBaseProps, FieldViewBaseProps } from '../forms/FormField'

export type TextViewProps = CommonProps<FieldViewBaseProps<string>>

export const TextView: React.FC<TextViewProps> = (props) => (
  <div {...common(props, ['TextView'])}>{props.value}</div>
)

export type TextEditProps = CommonProps<
  FieldEditBaseProps<string> & {
    placeholder?: string
    autoFocus?: boolean
  }
>

export const TextEdit: React.FC<TextEditProps> = (props) => {
  const onChange: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      props.onChange(event.target.value)
    },
    [props.onChange]
  )

  return (
    <label {...common(props)}>
      <input
        className="TextEdit__input"
        placeholder={props.placeholder}
        value={props.value}
        onChange={onChange}
        autoFocus={props.autoFocus}
      />
      <FieldErrors errors={props.errors} />
    </label>
  )
}

import React, { useCallback } from 'react'
import { baseProps, BaseProps } from '../baseProps'
import { FieldErrors } from '../forms/FieldErrors'
import { FieldEditBaseProps, FieldViewBaseProps } from '../forms/FormModel'

export type TextViewProps = BaseProps & FieldViewBaseProps<string>

export const TextView: React.FC<TextViewProps> = (props) => (
  <div {...baseProps(props, 'TextView')}>{props.value}</div>
)

export type TextEditProps = BaseProps &
  FieldEditBaseProps<string> & {
    placeholder?: string
    autoFocus?: boolean
  }

export const TextEdit: React.FC<TextEditProps> = (props) => {
  const onChange: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      props.onChange(event.target.value)
    },
    [props.onChange]
  )

  return (
    <label>
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

import bem from "bem-ts"
import React from "react"
import { InputContainer } from "./InputContainer"
import "./TextField.less"

const b = bem("textfield")

export type TextFieldProps = {
  value: string
  onChange: (value: string) => void
  onBlur?: () => void
  type?: "text" | "password"
  label?: React.ReactNode
  placeholder?: string
  disabled?: boolean
  icon?: React.ReactNode
  error?: React.ReactNode
  id?: string
}

export const TextField = (props: TextFieldProps) => (
  <InputContainer
    bemBase="textfield"
    label={props.label}
    icon={props.icon}
    error={props.error}
  >
    <input
      id={props.id}
      className={b("input", {
        error: Boolean(props.error),
      })}
      type={props.type || "type"}
      value={props.value}
      placeholder={props.placeholder}
      onChange={(event) => props.onChange(event.target.value)}
      onBlur={props.onBlur}
      disabled={props.disabled}
    />
  </InputContainer>
)

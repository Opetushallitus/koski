import bem from "bem-ts"
import React from "react"
import { InputContainer } from "./InputContainer"
import "./TextField.less"

const b = bem("textfield")

export type Props = {
  value: string
  onChange: (value: string) => void
  label?: string
  placeholder?: string
  disabled?: boolean
  icon?: React.ReactNode
}

export const TextField = (props: Props) => (
  <InputContainer bemBase="textfield" label={props.label} icon={props.icon}>
    <input
      className={b("input")}
      type="text"
      value={props.value}
      placeholder={props.placeholder}
      onChange={(event) => props.onChange(event.target.value)}
      disabled={props.disabled}
    />
  </InputContainer>
)

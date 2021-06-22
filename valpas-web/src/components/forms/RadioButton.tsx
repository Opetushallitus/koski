import bem from "bem-ts"
import React from "react"
import "./RadioButton.less"

const b = bem("radiobutton")

export type RadioButtonProps = {
  selected: boolean
  onChange: (selected: boolean) => void
  children: React.ReactNode
}

export const RadioButton = (props: RadioButtonProps) => (
  <label className={b("container")}>
    {props.children}
    <input
      type="radio"
      className={b("input")}
      checked={props.selected}
      onChange={(event) => props.onChange(event.target.checked)}
    />
    <span className={b("checkmark")}>
      <span className={b("checkmarkdot")} />
    </span>
  </label>
)

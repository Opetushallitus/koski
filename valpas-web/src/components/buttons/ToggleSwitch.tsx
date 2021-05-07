import bem from "bem-ts"
import React from "react"
import "./ToggleSwitch.less"

const b = bem("toggleswitch")

export type ToggleSwitchProps = {
  // checked: boolean
  // onChanged: (checked: boolean) => void
}

export const ToggleSwitch = (_props: ToggleSwitchProps) => (
  <label className={b("container")}>
    <input type="checkbox" />
    <span className={b("slider")} />
  </label>
)

import bem from "bem-ts"
import React from "react"
import "./ToggleSwitch.less"

const b = bem("toggleswitch")

export type ToggleSwitchProps = {
  value: boolean
  onChanged: (checked: boolean) => void
}

export const ToggleSwitch = (props: ToggleSwitchProps) => (
  <label className={b("container")}>
    <input
      type="checkbox"
      checked={props.value}
      onChange={(event) => props.onChanged(event.target.checked)}
    />
    <span className={b("slider")} />
  </label>
)

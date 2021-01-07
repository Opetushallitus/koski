import bem from "bem-ts"
import React from "react"
import "./Checkbox.less"

const b = bem("checkbox")

export type Props = Omit<
  React.HTMLAttributes<HTMLInputElement>,
  "value" | "onChange"
> & {
  value: boolean
  onChange: (selected: boolean) => void
}

// TODO: Tuunaa tästä hienompi
export const Checkbox = ({ value, onChange, ...rest }: Props) => (
  <input
    {...rest}
    className={b()}
    type="checkbox"
    checked={value}
    onChange={(event) => onChange(event.target.checked)}
  />
)

import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"
import "./Checkbox.less"

const b = bem("checkbox")

export type CheckboxProps = Omit<
  React.HTMLAttributes<HTMLInputElement>,
  "value" | "onChange"
> & {
  value: boolean
  onChange: (selected: boolean) => void
  disabled?: boolean
}

// TODO: Tuunaa tästä hienompi
export const Checkbox = ({ value, onChange, ...rest }: CheckboxProps) => (
  <input
    {...rest}
    className={b({ disabled: rest.disabled })}
    type="checkbox"
    checked={value}
    onChange={(event) => onChange(event.target.checked)}
  />
)

export type LabeledCheckboxProps = CheckboxProps & {
  label: string
}

export const LabeledCheckbox = ({
  label,
  className,
  ...checkboxProps
}: LabeledCheckboxProps) => (
  <label className={joinClassNames(className, b("label"))}>
    <Checkbox {...checkboxProps} />
    <div className={b("labeltext", { disabled: checkboxProps.disabled })}>
      {label}
    </div>
  </label>
)

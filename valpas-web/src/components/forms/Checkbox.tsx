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
  testId?: string
}

// TODO: Tuunaa tästä hienompi
export const Checkbox = ({
  value,
  onChange,
  testId,
  ...rest
}: CheckboxProps) => (
  <input
    {...rest}
    className={b({ disabled: rest.disabled })}
    type="checkbox"
    checked={value}
    onChange={(event) => onChange(event.target.checked)}
    data-testid={testId}
  />
)

export type LabeledCheckboxProps = CheckboxProps & {
  label: string
  inline?: boolean
  testId?: string
}

export const LabeledCheckbox = ({
  label,
  className,
  inline,
  ...checkboxProps
}: LabeledCheckboxProps) => (
  <label className={joinClassNames(className, b("label", { inline }))}>
    <Checkbox {...checkboxProps} />
    <div className={b("labeltext", { disabled: checkboxProps.disabled })}>
      {label}
    </div>
  </label>
)

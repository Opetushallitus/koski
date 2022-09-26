import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"
import "./buttons.less"

const b = bem("button")

export type DisableState = false | true | "byLook"

export type RaisedButtonProps = React.HTMLAttributes<HTMLButtonElement> & {
  hierarchy?: ButtonHierarchy
  disabled?: DisableState
  testId?: string
}

export type ButtonHierarchy = "primary" | "secondary" | "danger"

export const RaisedButton = (props: RaisedButtonProps) => {
  const { children, disabled, onClick, className, testId, ...rest } = props
  return (
    <button
      className={raisedButtonClassName(props)}
      onClick={disabled === true ? undefined : onClick}
      disabled={disabled === true}
      data-testid={testId}
      {...rest}
    >
      <span className={b("content")}>{children}</span>
    </button>
  )
}

export const raisedButtonClassName = ({
  hierarchy,
  disabled,
  className,
}: Pick<RaisedButtonProps, "hierarchy" | "disabled" | "className">) =>
  joinClassNames(
    b(["raised", hierarchy || "primary", disabled ? "disabled" : undefined]),
    className
  )

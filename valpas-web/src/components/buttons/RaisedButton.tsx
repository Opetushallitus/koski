import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"
import "./buttons.less"

const b = bem("button")

export type RaisedButtonProps = React.HTMLAttributes<HTMLButtonElement> & {
  hierarchy?: ButtonHierarchy
  disabled?: boolean
}

export type ButtonHierarchy = "primary" | "secondary"

export const RaisedButton = (props: RaisedButtonProps) => {
  const { children, disabled, onClick, ...rest } = props
  return (
    <button
      className={raisedButtonClassName(props)}
      onClick={disabled ? undefined : onClick}
      disabled={disabled}
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

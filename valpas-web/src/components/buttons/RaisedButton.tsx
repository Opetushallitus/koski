import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"
import "./buttons.less"

const b = bem("button")

export type RaisedButtonProps = React.HTMLAttributes<HTMLDivElement> & {
  hierarchy?: ButtonHierarchy
  disabled?: boolean
}

export type ButtonHierarchy = "primary" | "secondary"

export const RaisedButton = ({
  className,
  children,
  hierarchy,
  disabled,
  onClick,
  ...rest
}: RaisedButtonProps) => (
  <div
    className={joinClassNames(
      b(["raised", hierarchy || "primary", disabled ? "disabled" : undefined]),
      className
    )}
    onClick={disabled ? undefined : onClick}
    {...rest}
  >
    <span className={b("content")}>{children}</span>
  </div>
)

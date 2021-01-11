import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"
import "./buttons.less"

const b = bem("button")

export type FlatButtonProps = React.HTMLAttributes<HTMLDivElement> & {
  disabled?: boolean
}

export const FlatButton = ({
  className,
  children,
  onClick,
  disabled,
  ...rest
}: FlatButtonProps) => (
  <div
    className={joinClassNames(b({ disabled }), className)}
    onClick={disabled ? undefined : onClick}
    {...rest}
  >
    <span className={b("content")}>{children}</span>
  </div>
)

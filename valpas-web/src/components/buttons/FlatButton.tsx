import bem from "bem-ts"
import React from "react"
import { Link, LinkProps } from "react-router-dom"
import { joinClassNames } from "../../utils/classnames"
import "./buttons.less"

const b = bem("button")

export type FlatButtonProps = React.HTMLAttributes<HTMLButtonElement> & {
  disabled?: boolean
}

export const FlatButton = (props: FlatButtonProps) => {
  const { className, children, onClick, disabled, ...rest } = props
  return (
    <button
      className={flatButtonClassName(props)}
      onClick={disabled ? undefined : onClick}
      {...rest}
    >
      <span className={b("content")}>{children}</span>
    </button>
  )
}

export const FlatLink = (props: LinkProps) => {
  const { className, children, onClick, ...rest } = props
  return (
    <Link className={flatButtonClassName(props)} {...rest}>
      <span className={b("content")}>{children}</span>
    </Link>
  )
}

export const flatButtonClassName = ({
  disabled,
  className,
}: Pick<FlatButtonProps, "disabled" | "className">) =>
  joinClassNames(b(["flatten", disabled ? "disabled" : undefined]), className)

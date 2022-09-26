import bem from "bem-ts"
import React from "react"
import { Link, LinkProps } from "react-router-dom"
import { useBasePath } from "../../state/basePath"
import { joinClassNames } from "../../utils/classnames"
import "./buttons.less"

const b = bem("button")

export type FlatButtonProps = React.HTMLAttributes<HTMLButtonElement> & {
  disabled?: boolean
  testId?: string
}

export const FlatButton = (props: FlatButtonProps) => {
  const { className, children, onClick, disabled, testId, ...rest } = props
  return (
    <button
      className={flatButtonClassName(props)}
      onClick={disabled ? undefined : onClick}
      data-testid={testId}
      {...rest}
    >
      <span className={b("content")}>{children}</span>
    </button>
  )
}

export const FlatLink = (props: LinkProps) => {
  const { className, children, onClick, to, ...rest } = props
  const basePath = useBasePath()

  return (
    <Link className={flatButtonClassName(props)} to={basePath + to} {...rest}>
      <span className={b("content")}>{children}</span>
    </Link>
  )
}

export const flatButtonClassName = ({
  disabled,
  className,
}: Pick<FlatButtonProps, "disabled" | "className">) =>
  joinClassNames(b(["flatten", disabled ? "disabled" : undefined]), className)

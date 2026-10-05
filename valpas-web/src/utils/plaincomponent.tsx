import React from "react"
import { joinClassNames } from "./classnames"

export type PlainComponentProps = React.HTMLAttributes<HTMLElement>

export const plainComponent = (
  tag: keyof HTMLElementTagNameMap,
  baseClassName: string,
) => {
  const Plain = tag
  return ({ className, ...rest }: PlainComponentProps) => (
    <Plain className={joinClassNames(baseClassName, className)} {...rest} />
  )
}

export const forwardRefComponent = <T extends keyof HTMLElementTagNameMap>(
  tag: T,
  baseClassName: string,
) =>
  React.forwardRef(
    (
      { className, ...rest }: PlainComponentProps,
      ref: React.ForwardedRef<HTMLElementTagNameMap[T]>,
    ) =>
      React.createElement(tag, {
        className: joinClassNames(baseClassName, className),
        ...rest,
        ref,
      }),
  )

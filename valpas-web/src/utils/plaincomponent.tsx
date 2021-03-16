import React from "react"
import { joinClassNames } from "./classnames"

export type PlainComponentProps = React.HTMLAttributes<HTMLElement>

export const plainComponent = (tag: string, baseClassName: string) => {
  const Plain = tag
  return ({ className, ...rest }: PlainComponentProps) => (
    // @ts-ignore
    <Plain className={joinClassNames(baseClassName, className)} {...rest} />
  )
}

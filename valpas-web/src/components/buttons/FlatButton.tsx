import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"
import "./buttons.less"

const b = bem("button")

export type Props = React.HTMLAttributes<HTMLDivElement>

export const FlatButton = ({ className, children, ...rest }: Props) => (
  <div className={joinClassNames(b(), className)} {...rest}>
    <span className={b("content")}>{children}</span>
  </div>
)

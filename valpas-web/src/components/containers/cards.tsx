import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"
import "./cards.less"

const b = bem("card")

export type CardProps = React.HTMLAttributes<HTMLDivElement>

export const Card = ({ className, children, ...props }: CardProps) => (
  <section className={joinClassNames(b(), className)} {...props}>
    <div className={b("borders")}>{children}</div>
  </section>
)

export type CardHeaderProps = {
  children?: React.ReactNode
}

export const CardHeader = (props: CardHeaderProps) => (
  <header className={b("header")}>{props.children}</header>
)

export type CardBodyProps = React.HTMLAttributes<HTMLDivElement>

export const CardBody = ({ className, ...rest }: CardBodyProps) => (
  <div className={joinClassNames(b("body"), className)} {...rest} />
)

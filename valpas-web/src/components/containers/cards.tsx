import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"
import "./cards.less"

const b = bem("card")

export type CardProps = {
  children: React.ReactNode
}

export const Card = (props: CardProps) => (
  <section className={b()}>{props.children}</section>
)

export type CardHeaderProps = {
  children: string
}

export const CardHeader = (props: CardHeaderProps) => (
  <header className={b("header")}>{props.children}</header>
)

export type CardBodyProps = React.HTMLAttributes<HTMLDivElement>

export const CardBody = ({ className, ...rest }: CardBodyProps) => (
  <div className={joinClassNames(b("body"), className)} {...rest} />
)

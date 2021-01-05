import bem from "bem-ts"
import React from "react"
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

export type CardBodyProps = {
  children: React.ReactNode
}

export const CardBody = (props: CardBodyProps) => (
  <div className={b("body")}>{props.children}</div>
)

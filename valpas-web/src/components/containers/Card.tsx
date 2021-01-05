import bem from "bem-ts"
import React from "react"
import "./Card.less"

export type Props = {
  children: React.ReactNode
}

const b = bem("card")

export const CardFrame = (props: Props) => (
  <section className={b()}>{props.children}</section>
)

export const Card = (props: Props) => (
  <section className={b(["padded"])}>{props.children}</section>
)

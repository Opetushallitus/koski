import React from "react"
import bem from "bem-ts"
import "./headings.less"

export type Props = {
  children: React.ReactNode
}

const b = bem("heading")

export const Heading = (props: Props) => (
  <h1 className={b(["primary"])}>{props.children}</h1>
)

export const SecondaryHeading = (props: Props) => (
  <h1 className={b(["secondary"])}>{props.children}</h1>
)

import bem from "bem-ts"
import React from "react"
import "./headings.less"

export type HeadingProps = {
  children: React.ReactNode
}

const b = bem("heading")

export const Heading = (props: HeadingProps) => (
  <h1 className={b(["primary"])}>{props.children}</h1>
)

export const SecondaryHeading = (props: HeadingProps) => (
  <h1 className={b(["secondary"])}>{props.children}</h1>
)

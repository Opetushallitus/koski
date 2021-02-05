import bem from "bem-ts"
import React from "react"
import "./ButtonLabel.less"

const b = bem("buttonlabel")

export type ButtonLabelProps = {
  children: React.ReactNode
}

export const ButtonLabel = (props: ButtonLabelProps) => (
  <span className={b()}>{props.children}</span>
)

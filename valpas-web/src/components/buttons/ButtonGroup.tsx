import bem from "bem-ts"
import React from "react"
import "./ButtonGroup.less"

const b = bem("buttongroup")

export type ModalButtonGroupProps = {
  children: React.ReactNode
}

export const ButtonGroup = (props: ModalButtonGroupProps) => (
  <div className={b()}>{props.children}</div>
)

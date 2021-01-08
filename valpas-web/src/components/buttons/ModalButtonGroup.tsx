import bem from "bem-ts"
import React from "react"
import "./ModalButtonGroup.less"

const b = bem("modalbuttongroup")

export type ModalButtonGroupProps = {
  children: React.ReactNode
}

export const ModalButtonGroup = (props: ModalButtonGroupProps) => (
  <div className={b()}>{props.children}</div>
)

import bem from "bem-ts"
import React from "react"
import "./ModalButtonGroup.less"

const b = bem("modalbuttongroup")

export type Props = {
  children: React.ReactNode
}

export const ModalButtonGroup = (props: Props) => (
  <div className={b()}>{props.children}</div>
)

import bem from "bem-ts"
import React from "react"
import { joinClassNames } from "../../utils/classnames"
import "./ButtonGroup.less"

const b = bem("buttongroup")

export type ModalButtonGroupProps = {
  children: React.ReactNode
  className?: string
}

export const ButtonGroup = (props: ModalButtonGroupProps) => (
  <div className={joinClassNames(b(), props.className)}>{props.children}</div>
)

import bem from "bem-ts"
import React from "react"
import "./NoDataMessage.less"

const b = bem("nodatamessage")

export type NoDataMessageProps = {
  children: React.ReactNode
}

export const NoDataMessage = (props: NoDataMessageProps) => (
  <div className={b()}>{props.children}</div>
)

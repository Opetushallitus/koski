import bem from "bem-ts"
import React from "react"
import "./Error.less"

const b = bem("error")

export type ErrorProps = {
  children: React.ReactNode
}

export const Error = (props: ErrorProps) => (
  <div className={b()}>{props.children}</div>
)

import bem from "bem-ts"
import React from "react"
import "./Counter.less"

const b = bem("counter")

export type CounterProps = {
  children: React.ReactNode
}

export const Counter = (props: CounterProps) => (
  <span className={b()}>
    <span className={b("paren")}> (</span>
    {props.children}
    <span className={b("paren")}>)</span>
  </span>
)

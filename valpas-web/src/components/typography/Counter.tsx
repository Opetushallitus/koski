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

export type NumberCounterProps = {
  value: number
  max?: number
}

export const NumberCounter = (props: NumberCounterProps) => (
  <Counter>
    {props.value === props.max || props.max === undefined
      ? props.value
      : `${props.value} / ${props.max}`}
  </Counter>
)

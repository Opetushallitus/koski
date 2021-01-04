import React from "react"

export type GreetingProps = {
  children: string
}

export const Greeting = (props: GreetingProps) => (
  <h1>Hello, {props.children}!</h1>
)

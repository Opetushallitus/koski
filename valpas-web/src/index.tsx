import React from "react"
import ReactDOM from "react-dom"

type GreetingProps = {
  children: string
}

const Greeting = (props: GreetingProps) => <h1>Hello, {props.children}!</h1>

ReactDOM.render(<Greeting>world</Greeting>, document.getElementById("app"))

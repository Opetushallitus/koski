import React from "react"
import renderer from "react-test-renderer"
import { Greeting } from "./Greeting"

test("Greeting tervehtii oikein", () => {
  const component = renderer.create(<Greeting>world</Greeting>)
  const greeting = component.toJSON()
  expect(greeting).toMatchInlineSnapshot(`
    <h1>
      Hello, 
      world
      !
    </h1>
  `)
})

import { RenderResult } from "@testing-library/react"

export const expectToMatchSnapshot = (element: RenderResult) =>
  expect(element.container.firstChild).toMatchSnapshot()

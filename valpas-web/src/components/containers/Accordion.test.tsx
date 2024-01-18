import { fireEvent, render } from "@testing-library/react"
import React from "react"
import { expectToMatchSnapshot } from "../../utils/tests"
import { Accordion } from "./Accordion"

describe("Accordion", () => {
  test("renderöityy oikein", () => {
    expectToMatchSnapshot(createAccordion(testContent))
  })

  test("kaikki sivut avatutuvat klikkauksesta", async () => {
    const accordion = createAccordion(testContent)

    testLabels.map((labelText) =>
      fireEvent.click(accordion.getByText(labelText)),
    )

    expectToMatchSnapshot(accordion)
  })
})

// Helpers

const testContent: Record<string, string> = {
  Yksi: "Ekan sivun sisältö",
  Kaksi: "Tokan sivun sisältö",
  Kolmas: "Kolmannen sivun sisältö",
}

const testLabels = Object.keys(testContent)

const createAccordion = (items: Record<string, string>) =>
  render(
    <Accordion
      accordionId="test"
      items={Object.entries(items).map(([label, content]) => ({
        label,
        render: () => content,
      }))}
    />,
  )

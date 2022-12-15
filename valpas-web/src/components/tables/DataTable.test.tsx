import { fireEvent, render, RenderResult } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import React from "react"
import { disableMissingTranslationWarnings } from "../../i18n/i18n"
import { Column, DataTable, Datum } from "./DataTable"

describe("DataTable", () => {
  test("Aktiivisen sarakkeen nimen uudelleen klikkaaminen kääntää järjestyksen", () => {
    const table = createTable()

    clickColumnLabel(table, "Nimi")
    expectOrderOfValuesToBe(table, 1, [
      "Zorro #10",
      "Zorro #9",
      "Osmo",
      "Heli",
      "Heikki",
      "Aatu",
    ])

    clickColumnLabel(table, "Nimi")
    expectOrderOfValuesToBe(table, 1, [
      "Aatu",
      "Heikki",
      "Heli",
      "Osmo",
      "Zorro #9",
      "Zorro #10",
    ])
  })

  test("Toisen sarakkeen nimen klikkaaminen järjestää sen mukaisesti", () => {
    const table = createTable()
    clickColumnLabel(table, "Oppilaitos")
    expectOrderOfValuesToBe(table, 2, [
      "Perustestikoulu",
      "Perustestikoulu",
      "Testiperuskoulu",
      "Testiperuskoulu",
      "Zorrokoulu",
      "Zorrokoulu",
    ])
  })

  test("Vapaatekstihaku toimii", async () => {
    const table = createTable()
    await fillTextFilter(table, 1, "he")
    expectOrderOfValuesToBe(table, 1, ["Heikki", "Heli"])
  })

  test("Pudotusvalikkosuodatin toimii", async () => {
    const table = createTable()
    await selectDropdownOption(table, 2, 2)
    expectOrderOfValuesToBe(table, 1, ["Heli", "Osmo"])
  })

  test("Useamman suodattimen käyttö suodattaa AND-logiikalla", async () => {
    const table = createTable()
    await fillTextFilter(table, 1, "he")
    await selectDropdownOption(table, 2, 2)
    expectOrderOfValuesToBe(table, 1, ["Heli"])
  })

  test("filterValues-property yliajaa valuen, jos se on määritelty", async () => {
    const table = createTable()
    await fillTextFilter(table, 1, "cosmic")
    expectOrderOfValuesToBe(table, 1, ["Osmo", "Zorro #9"])
  })

  test("filterValues-property yliajaa valuen, jos se on määritelty, osa 2", async () => {
    const table = createTable()
    await fillTextFilter(table, 1, "avenger")
    expectOrderOfValuesToBe(table, 1, ["Zorro #9", "Zorro #10"])
  })
})

// Helpers

const createTable = () => {
  disableMissingTranslationWarnings()
  return render(<DataTable columns={columns} data={data} />)
}

const expectOrderOfValuesToBe = (
  table: RenderResult,
  columnIndex: number,
  expectedValues: string[]
) => {
  const values: string[] = []
  table.container
    .querySelectorAll(`tbody td:nth-child(${columnIndex})`)
    .forEach((td) => {
      values.push(td.textContent || "")
    })
  expect(values).toEqual(expectedValues)
}

const clickColumnLabel = (table: RenderResult, columnLabel: string) =>
  fireEvent.click(table.getByText(columnLabel))

const fillTextFilter = async (
  table: RenderResult,
  columnIndex: number,
  text: string
) => {
  const input = table.container.querySelector(
    `thead tr:nth-child(2) th:nth-child(${columnIndex}) input`
  )
  expect(input).not.toBeNull()
  await userEvent.type(input!!, text)
}

const selectDropdownOption = async (
  table: RenderResult,
  columnIndex: number,
  optionIndex: number
) => {
  const select = table.container.querySelector(
    `thead tr:nth-child(2) th:nth-child(${columnIndex}) select`
  )
  expect(select).not.toBeNull()
  await userEvent.selectOptions(select!!, optionIndex.toString())
}

// Test data

const columns: Column[] = [
  { label: "Nimi", filter: "freetext" },
  { label: "Oppilaitos", filter: "dropdown" },
]
const data: Datum[] = [
  {
    key: ["1"],
    values: [{ value: "Heikki" }, { value: "Perustestikoulu" }],
  },
  {
    key: ["2"],
    values: [{ value: "Heli" }, { value: "Testiperuskoulu" }],
  },
  {
    key: ["3"],
    values: [{ value: "Aatu" }, { value: "Perustestikoulu" }],
  },
  {
    key: ["4"],
    values: [
      { value: "Osmo", filterValues: ["cosmic"] },
      { value: "Testiperuskoulu" },
    ],
  },
  {
    key: ["5"],
    values: [
      {
        value: "zorro-09",
        display: "Zorro #9",
        filterValues: ["cosmic", "avenger"],
      },
      { value: "Zorrokoulu" },
    ],
  },
  {
    key: ["6"],
    values: [
      { value: "zorro-10", display: "Zorro #10", filterValues: ["avenger"] },
      { value: "Zorrokoulu" },
    ],
  },
]

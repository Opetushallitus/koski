import { fireEvent, render, RenderResult } from "@testing-library/react"
import React from "react"
import { expectToMatchSnapshot } from "../../utils/tests"
import { Column, DataTable, Datum } from "./DataTable"

describe("DataTable", () => {
  test("Renderöityy oikein", () => {
    expectToMatchSnapshot(createTable())
  })

  test("Aktiivisen sarakkeen nimen uudelleen klikkaaminen kääntää järjestyksen", () => {
    const table = createTable()
    clickColumnLabel(table, "Nimi")
    expectToMatchSnapshot(table)
  })

  test("Toisen sarakkeen nimen klikkaaminen järjestää sen mukaisesti", () => {
    const table = createTable()
    clickColumnLabel(table, "Oppilaitos")
    expectToMatchSnapshot(table)
  })
})

// Helpers

const createTable = () => render(<DataTable columns={columns} data={data} />)

const clickColumnLabel = (table: RenderResult, columnLabel: string) =>
  fireEvent.click(table.getByText(columnLabel))

// Test data

const columns: Column[] = [{ label: "Nimi" }, { label: "Oppilaitos" }]
const data: Datum[] = [
  {
    key: ["1"],
    values: [{ value: "Heikki" }, { value: "Aakkoskoulu" }],
  },
  {
    key: ["2"],
    values: [{ value: "Heli" }, { value: "Beetakaroteenikoulu" }],
  },
  {
    key: ["3"],
    values: [{ value: "Aatu" }, { value: "Deltakoulu" }],
  },
  {
    key: ["4"],
    values: [{ value: "Osmo" }, { value: "Celsiuksen koulu" }],
  },
]

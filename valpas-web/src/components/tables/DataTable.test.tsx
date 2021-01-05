import React from "react"
import renderer, { act } from "react-test-renderer"
import { Column, DataTable, Datum } from "./DataTable"

describe("DataTable", () => {
  test("Renderöityy oikein", () => {
    expect(createTable().toJSON()).toMatchSnapshot()
  })

  test("Aktiivisen sarakkeen nimen uudelleen klikkaaminen kääntää järjestyksen", () => {
    const table = createTable()
    clickColumnLabel(table, 0)
    expect(table.toJSON()).toMatchSnapshot()
  })

  test("Toisen sarakkeen nimen klikkaaminen järjestää sen mukaisesti", () => {
    const table = createTable()
    clickColumnLabel(table, 1)
    expect(table.toJSON()).toMatchSnapshot()
  })
})

// Helpers

const createTable = () =>
  renderer.create(<DataTable columns={columns} data={data} />)

const clickColumnLabel = (table: renderer.ReactTestRenderer, index: number) =>
  act(() =>
    // @ts-ignore
    table.root
      .findAll((e) => e.props.className?.includes("table__label"))
      [index].props.onClick()
  )

// Test data

const columns: Column[] = [{ label: "Nimi" }, { label: "Oppilaitos" }]
const data: Datum[] = [
  {
    key: "1",
    values: [{ value: "Heikki" }, { value: "Aakkoskoulu" }],
  },
  {
    key: "2",
    values: [{ value: "Heli" }, { value: "Beetakaroteenikoulu" }],
  },
  {
    key: "3",
    values: [{ value: "Aatu" }, { value: "Deltakoulu" }],
  },
  {
    key: "4",
    values: [{ value: "Osmo" }, { value: "Celsiuksen koulu" }],
  },
]

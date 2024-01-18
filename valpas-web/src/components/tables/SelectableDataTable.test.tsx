import { render, RenderResult } from "@testing-library/react"
import userEvent from "@testing-library/user-event"
import React from "react"
import { Column, Datum, DatumKey } from "./DataTable"
import { SelectableDataTable } from "./SelectableDataTable"

describe("SelectableDataTable", () => {
  test("rivien valinta toimii oikein", async () => {
    const onSelect = jest.fn()
    const table = createTable(
      stringsToData([
        "Aapeli",
        "Bertta",
        "Cecilia",
        "Daavid",
        "Erkki",
        "Faarao",
        "Gabriel",
      ]),
      onSelect,
    )

    await clickRow(table, 1)
    await clickRow(table, 3)
    await clickRow(table, 5)
    await clickRow(table, 3)

    expect(onSelect).toHaveBeenCalledTimes(4)
    expect(onSelect).toHaveBeenNthCalledWith(1, [["1", "aapeli"]])
    expect(onSelect).toHaveBeenNthCalledWith(2, [
      ["1", "aapeli"],
      ["3", "cecilia"],
    ])
    expect(onSelect).toHaveBeenNthCalledWith(3, [
      ["1", "aapeli"],
      ["3", "cecilia"],
      ["5", "erkki"],
    ])
    expect(onSelect).toHaveBeenNthCalledWith(4, [
      ["1", "aapeli"],
      ["5", "erkki"],
    ])
  })

  test("monivalitse rivit, jotka ovat annetun yhtäläisyysfunktion mukaan yhtäläiset rivit", async () => {
    const equalNames = (a: DatumKey) => (b: DatumKey) => a[1] === b[1]
    const onSelect = jest.fn()

    const table = createTable(
      stringsToData(["alfa", "alfa", "beta", "gamma"]),
      onSelect,
      equalNames,
    )
    await clickRow(table, 1)
    await clickRow(table, 2)

    expect(onSelect).toHaveBeenCalledTimes(2)
    expect(onSelect).toHaveBeenNthCalledWith(1, [
      ["1", "alfa"],
      ["2", "alfa"],
    ])
    expect(onSelect).toHaveBeenNthCalledWith(2, [])
  })
})

// Helpers

const createTable = (
  data: Datum[],
  onSelect: (selectedKeys: DatumKey[]) => void,
  peerEquality?: (a: DatumKey) => (b: DatumKey) => boolean,
) =>
  render(
    <SelectableDataTable
      columns={columns}
      data={data}
      onSelect={onSelect}
      peerEquality={peerEquality}
    />,
  )

const clickRow = async (table: RenderResult, nthRow: number) => {
  const checkbox = table.container.querySelector(
    `tbody tr:nth-child(${nthRow}) td:first-child input`,
  )
  expect(checkbox).not.toBeNull()
  await userEvent.click(checkbox!!)
}

// Test data

const columns: Column[] = [
  {
    label: "Nimi",
  },
]

const stringsToData = (values: string[]): Datum[] =>
  values.map(
    (value, index): Datum => ({
      key: [(index + 1).toString(), value.toLowerCase()],
      values: [{ value }],
    }),
  )

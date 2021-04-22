import * as A from "fp-ts/Array"
import { $$ } from "./core"
import { eventually } from "./utils"

export const dataTableEventuallyEquals = async (
  selector: string,
  displayValues: string,
  columnSeparator = "\t",
  timeout = 1000
) => {
  await dataTableCellsEventuallyEquals(
    `${selector} .table__body .table__td`,
    displayValues,
    columnSeparator,
    timeout
  )
}

export const dataTableHeadersEventuallyEquals = async (
  selector: string,
  displayValues: string,
  columnSeparator = "\t",
  timeout = 1000
) => {
  await dataTableCellsEventuallyEquals(
    `${selector} .table__body .table__th`,
    displayValues,
    columnSeparator,
    timeout
  )
}

const dataTableCellsEventuallyEquals = async (
  selector: string,
  displayValues: string,
  columnSeparator = "\t",
  timeout = 1000
) => {
  const expectedData = A.flatten(
    displayValues
      .split("\n")
      .map((row) => row.trim())
      .filter((row) => row.length > 0)
      .map((row) => row.split(columnSeparator).map((c) => c.trim()))
  )

  await eventually(async () => {
    const cells = await $$(`${selector}`)
    const actualData = (
      await Promise.all(cells.map((cell) => cell.getText()))
    ).map((value) => value.replace(/\n/g, ""))
    expect(actualData).toEqual(expectedData)
  }, timeout)
}

import * as A from "fp-ts/Array"
import { By } from "selenium-webdriver"
import { $, $$ } from "./core"
import { driver } from "./driver"
import { clearTextInput, setTextInput } from "./forms"
import { mediumTimeout, shortTimeout } from "./timeouts"
import { eventually } from "./utils"

export const dataTableEventuallyEquals = async (
  selector: string,
  displayValues: string,
  columnSeparator = "\t",
  timeout = mediumTimeout
) => {
  await waitTableLoadingHasFinished(selector)
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
  timeout = shortTimeout
) => {
  await waitTableLoadingHasFinished(selector)
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
  timeout = shortTimeout
) => {
  const expectedData = A.flatten(
    displayValues
      .split("\n")
      .map((row) => row.trim())
      .filter((row) => row.length > 0)
      .map((row) => row.split(columnSeparator).map((c) => c.trim()))
  )

  await eventually(async () => {
    const actualData = await getTableContents(selector)
    expect(actualData).toEqual(expectedData)
  }, timeout)
}

export const getTableContents = async (selector: string) => {
  const cells = await $$(`${selector}`)
  return (await Promise.all(cells.map((cell) => cell.getText()))).map((value) =>
    value.replace(/\n/g, "")
  )
}

export const setTableTextFilter = async (
  selector: string,
  nthColumn: number,
  filterValue: string
) => {
  const inputSelector = `${selector} .table__head .table__row:nth-child(2) .table__filter:nth-child(${nthColumn}) input`
  await clearTextInput(inputSelector)
  await setTextInput(inputSelector, filterValue)
}

export const toggleTableSort = async (selector: string, nthColumn: number) => {
  const labelSelector = `${selector} .table__head .table__row:first-child .table__label:nth-child(${nthColumn})`
  const label = await $(labelSelector)
  await label.click()
}

export const getExpectedRowCount = (displayValues: string) =>
  displayValues.split("\n").filter((row) => row.trim().length > 0).length

export const waitTableLoadingHasFinished = async (tableSelector: string) => {
  const selector = `${tableSelector} .spinner`
  eventually(async () => {
    expect((await driver.findElements(By.css(selector))).length).toEqual(0)
  })
}

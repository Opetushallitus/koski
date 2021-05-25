import { Key, WebElement } from "selenium-webdriver"
import { attributeEventuallyEquals } from "./content"
import { $, $$ } from "./core"
import { driver } from "./driver"
import { eventually } from "./utils"

export const setTextInput = async (selector: string, value: string) => {
  await clearTextInput(selector)

  const element = await $(selector)
  await element.sendKeys(value, Key.ENTER)

  await attributeEventuallyEquals(selector, "value", value)
}

export const getTextInput = async (selector: string) => {
  const element = await $(selector)
  return element.getAttribute("value")
}

export const clearTextInput = async (selector: string, timeout = 1000) =>
  clearTextInputElement(await $(selector), timeout)

export const clearTextInputElement = async (
  element: WebElement,
  timeout = 1000
) =>
  // Pitää tehdä silmukassa, koska tämä ei aina toimi, välillä BACK_SPACE poistaa vain viimeisen merkin
  eventually(async () => {
    await driver.executeScript((element: any) => element.select(), element)
    await element.sendKeys(Key.BACK_SPACE)
    expect(await element.getAttribute("value")).toEqual("")
  }, timeout)

export const dropdownSelect = async (selector: string, index: number) => {
  const optionSelector = `${selector} > option[value='${index}']`
  const option = await $(optionSelector)
  option.click()
}

export const dropdownSelectContains = async (
  selector: string,
  text: string
) => {
  const allOptionsSelector = `${selector} > option`
  const options = await $$(allOptionsSelector)
  for (const option of options) {
    const optionText = await option.getText()
    if (optionText.includes(text)) {
      await option.click()
      break
    }
  }
}

export const isCheckboxChecked = async (selector: string): Promise<boolean> => {
  const input = await $(selector)
  return input.getAttribute("checked").then((c) => !!c)
}

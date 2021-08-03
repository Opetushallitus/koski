import { By } from "selenium-webdriver"
import { $ } from "./core"
import { driver } from "./driver"
import { defaultTimeout } from "./timeouts"
import { eventually } from "./utils"

export const textEventuallyEquals = (
  selector: string,
  expected: string,
  timeout = defaultTimeout
) =>
  eventually(async () => {
    const element = await $(selector)
    expect(await element.getText()).toEqual(expected)
  }, timeout)

export const contentEventuallyEquals = (
  selector: string,
  expected: string,
  timeout = defaultTimeout
) =>
  textEventuallyEquals(
    selector,
    expected
      .trim()
      .split("\n")
      .map((a) => a.trim().replace(/\s+/g, " "))
      .join("\n"),
    timeout
  )

export const attributeEventuallyEquals = (
  selector: string,
  attributeName: string,
  expected: string,
  timeout = defaultTimeout
) =>
  eventually(async () => {
    const element = await $(selector)
    expect(await element.getAttribute(attributeName)).toEqual(expected)
  }, timeout)

export const expectElementEventuallyVisible = async (selector: string) => {
  eventually(async () => {
    const elements = await driver.findElements(By.css(selector))
    expect(
      elements.length > 0,
      `Element ${selector} expected to exist`
    ).toBeTruthy()
  })
}

export const expectElementVisible = async (selector: string) => {
  const elements = await driver.findElements(By.css(selector))
  expect(
    elements.length > 0,
    `Element ${selector} expected to exist`
  ).toBeTruthy()
}

export const expectElementNotVisible = async (selector: string) => {
  const elements = await driver.findElements(By.css(selector))
  expect(
    elements.length === 0,
    `Element ${selector} expected NOT to exist`
  ).toBeTruthy()
}

export const clickElement = async (selector: string) => {
  const element = await $(selector)

  expect(
    await element.isEnabled(),
    `Element ${selector} expected to be enabled`
  )

  await element.click()
}

import { By } from "selenium-webdriver"
import { $ } from "./core"
import { driver } from "./driver"
import { defaultTimeout } from "./timeouts"
import { eventually } from "./utils"
import "jest-expect-message"

export const textEventuallyEquals = (
  selector: string,
  expected: string,
  timeout = defaultTimeout,
) =>
  eventually(async () => {
    const element = await $(selector)
    expect(await element.getText()).toEqual(expected)
  }, timeout)

export const testId = <Value extends string>(
  value: Value,
): `[data-testid="${Value}"]` => `[data-testid="${value}"]`

export const contentEventuallyEquals = (
  selector: string,
  expected: string,
  timeout = defaultTimeout,
) =>
  textEventuallyEquals(
    selector,
    expected
      .trim()
      .split("\n")
      .map((a) => a.trim().replace(/\s+/g, " "))
      .join("\n"),
    timeout,
  )

export const attributeEventuallyEquals = (
  selector: string,
  attributeName: string,
  expected: string,
  timeout = defaultTimeout,
) =>
  eventually(async () => {
    const element = await $(selector)
    expect(await element.getAttribute(attributeName)).toEqual(expected)
  }, timeout)

export const expectElementEventuallyVisible = async (
  selector: string,
  timeout = defaultTimeout,
) => {
  await eventually(async () => {
    const elements = await driver.findElements(By.css(selector))
    expect(
      elements.length > 0,
      `Element ${selector} expected to exist`,
    ).toBeTruthy()
  }, timeout)
}

export const expectElementVisible = async (selector: string) => {
  const elements = await driver.findElements(By.css(selector))
  expect(
    elements.length > 0,
    `Element ${selector} expected to exist`,
  ).toBeTruthy()
}

export const expectElementEventuallyNotVisible = async (selector: string) => {
  await eventually(async () => {
    const elements = await driver.findElements(By.css(selector))
    expect(
      elements.length === 0,
      `Element ${selector} expected NOT to exist`,
    ).toBeTruthy()
  })
}

export const expectElementByTextEventuallyNotVisible = async (text: string) => {
  await eventually(async () => {
    const elements = await driver.findElements(
      By.xpath(`//*[contains(text(), '${text}')]`),
    )
    expect(
      elements.length === 0,
      `Element by text "${text}" expected NOT to exist`,
    ).toBeTruthy()
  })
}

export const expectElementNotVisible = async (selector: string) => {
  const elements = await driver.findElements(By.css(selector))
  expect(
    elements.length === 0,
    `Element ${selector} expected NOT to exist`,
  ).toBeTruthy()
}

export const clickElement = async (selector: string) => {
  const element = await $(selector)

  expect(
    await element.isEnabled(),
    `Element ${selector} expected to be enabled`,
  )

  await element.click()
}

export const expectLinkToEqual = async (selector: string, href: string) => {
  await eventually(async () => {
    const link = await $(selector)
    expect(await link.getAttribute("href")).toEqual(href)
  })
}

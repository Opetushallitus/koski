import { By } from "selenium-webdriver"
import { $ } from "./core"
import { driver } from "./driver"
import { defaultTimeout } from "./timeouts"
import { eventually, withMessage } from "./utils"

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
    withMessage(`Element ${selector} expected to exist`, () =>
      expect(elements.length).toBeGreaterThan(0),
    )
  }, timeout)
}

export const expectElementVisible = async (selector: string) => {
  const elements = await driver.findElements(By.css(selector))
  withMessage(`Element ${selector} expected to exist`, () =>
    expect(elements.length).toBeGreaterThan(0),
  )
}

export const expectElementEventuallyNotVisible = async (selector: string) => {
  await eventually(async () => {
    const elements = await driver.findElements(By.css(selector))
    withMessage(`Element ${selector} expected NOT to exist`, () =>
      expect(elements.length).toBe(0),
    )
  })
}

export const expectElementByTextEventuallyNotVisible = async (text: string) => {
  await eventually(async () => {
    const elements = await driver.findElements(
      By.xpath(`//*[contains(text(), '${text}')]`),
    )
    withMessage(`Element by text "${text}" expected NOT to exist`, () =>
      expect(elements.length).toBe(0),
    )
  })
}

export const expectElementNotVisible = async (selector: string) => {
  const elements = await driver.findElements(By.css(selector))
  withMessage(`Element ${selector} expected NOT to exist`, () =>
    expect(elements.length).toBe(0),
  )
}

export const clickElement = async (selector: string) => {
  const element = await $(selector)

  await element.click()
}

export const expectLinkToEqual = async (selector: string, href: string) => {
  await eventually(async () => {
    const link = await $(selector)
    expect(await link.getAttribute("href")).toEqual(href)
  })
}

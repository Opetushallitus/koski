import { By, Condition, until } from "selenium-webdriver"
import { driver } from "./driver"
import { eventually } from "./utils"

const wait = async <T>(condition: Condition<T>, timeout: number) => {
  return await driver.wait(async (d) => condition.fn(d), timeout)
}

export const goToLocation = async (path: string) => {
  await driver.get(
    `http://localhost:1234${process.env.PUBLIC_URL || ""}${path}`
  )
}

export const deleteCookies = async () => {
  await driver.manage().deleteAllCookies()
}

export const getCurrentUrl = () => driver.getCurrentUrl()

export const urlIsEventually = async (
  expectedUrl: string,
  timeout: number = 10000
) => {
  try {
    await eventually(async () => {
      expect(await getCurrentUrl()).toMatch(expectedUrl)
    }, timeout)
  } catch (error) {
    throw new Error(
      `Expected URL eventually to be ${expectedUrl}. It is currently ${await getCurrentUrl()}`
    )
  }
}

export const pathToUrl = (path: string) => `http://localhost:1234/valpas${path}`

export const pathToApiUrl = (path: string) =>
  `http://localhost:1234/koski/valpas${path}`

export const $ = async (selector: string, timeout = 200) => {
  try {
    const el = await wait(until.elementLocated(By.css(selector)), timeout)
    return await wait(until.elementIsVisible(el), timeout)
  } catch (_err) {
    throw new Error(`Could not find a visible element by "${selector}"`)
  }
}

export const $$ = async (selector: string, timeout = 200) => {
  try {
    return await wait(until.elementsLocated(By.css(selector)), timeout)
  } catch (_err) {
    throw new Error(`Could not find elements by "${selector}"`)
  }
}

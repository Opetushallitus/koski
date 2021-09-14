import fs from "fs/promises"
import { By, Condition, until, WebElement } from "selenium-webdriver"
import { Feature } from "../../../src/state/featureFlags"
import { driver } from "./driver"
import { defaultSleepTime, defaultTimeout, shortTimeout } from "./timeouts"
import { eventually, sleep } from "./utils"

const wait = async <T>(condition: Condition<T>, timeout: number) => {
  return await driver.wait(async (d) => condition.fn(d), timeout)
}

export const goToLocation = async (path: string) => {
  await driver.get(
    // TODO: Tutki miksi PUBLIC_URL ei asetu enää tänne
    `http://localhost:1234${process.env.PUBLIC_URL || "/valpas"}${path}` +
      featureQuery()
  )
}

export const deleteCookies = async () => {
  await eventually(async () => {
    await driver.manage().deleteAllCookies()
    const cookies = await driver.manage().getCookies()
    expect(cookies.length, "Expected all cookies to be deleted").toBe(0)
  })
}

export const getCurrentUrl = () => driver.getCurrentUrl()

export const urlIsEventually = async (
  expectedUrl: string,
  timeout: number = defaultTimeout
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

export const $ = async (selector: string, timeout = shortTimeout) => {
  try {
    const el = await wait(until.elementLocated(By.css(selector)), timeout)
    return await wait(until.elementIsVisible(el), timeout)
  } catch (_err) {
    throw new Error(`Could not find a visible element by "${selector}"`)
  }
}

export const $$ = async (selector: string, timeout = shortTimeout) => {
  try {
    return await wait(until.elementsLocated(By.css(selector)), timeout)
  } catch (_err) {
    throw new Error(`Could not find elements by "${selector}"`)
  }
}

export const testIdIs = (testId: string) => By.css(`[data-testid="${testId}"]`)

export const scrollIntoView = async (element: WebElement) => {
  await driver.executeScript("arguments[0].scrollIntoView(true);", element)
  await sleep(defaultSleepTime)
}

export const takeScreenshot = async (filename: string) => {
  const image = await driver.takeScreenshot()
  console.log(`Saving screenshot to ${filename}`)
  await fs.writeFile(filename, image, "base64")
}

export const getText = (webElement: WebElement) => webElement.getText()

export const getOptionalText = async (webElements: WebElement[]) =>
  webElements[0]?.getText()

// Mekanismi featureflagien väliaikaiseen sulkemiseen

const disabledFeatures = new Set<Feature>()

export const resetFeatures = () => {
  disabledFeatures.clear()
}

export const disableFeature = (feature: Feature) => {
  disabledFeatures.add(feature)
}

const featureQuery = () => {
  const collection: Feature[] = []
  disabledFeatures.forEach((f) => collection.push(f))
  return collection.length === 0
    ? ""
    : "?" + collection.map((f) => `disable-${f}`).join("&")
}

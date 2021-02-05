import {
  Builder,
  WebDriver,
  until,
  By,
  Key,
  Condition,
} from "selenium-webdriver"
import chrome from "selenium-webdriver/chrome"
import "chromedriver"
import * as A from "fp-ts/Array"
import { expectCleanConsoleLogs } from "./fail-on-console"

declare namespace global {
  let __driver__: undefined | (() => Promise<WebDriver>)
}

let driver: WebDriver
jasmine.DEFAULT_TIMEOUT_INTERVAL = 1000 * 60 * 5

beforeAll(async () => {
  driver = (await buildBrowserStackDriver()) || (await buildChromeDriver())
}, 20000)

afterAll(() => driver?.quit())

afterEach(async () => {
  await expectCleanConsoleLogs(driver)
})

const buildBrowserStackDriver = async (): Promise<WebDriver | undefined> =>
  // Browserstack webdriver is provided by jest-environment-browserstack
  // See jest.integrationtests.browserstack.config.js
  global.__driver__ && global.__driver__()

const buildChromeDriver = async (): Promise<WebDriver> => {
  const builder = new Builder().forBrowser("chrome")
  if (!process.env.SHOW_BROWSER) {
    builder.setChromeOptions(new chrome.Options().headless())
  }
  return builder.build()
}

// Helpers

const wait = async <T>(condition: Condition<T>, timeout: number) => {
  return await driver.wait(async (d) => condition.fn(d), timeout)
}

export const goToLocation = async (path: string) => {
  await driver.get(
    `http://localhost:1234${process.env.PUBLIC_URL || ""}${path}`
  )
}

export const $ = async (selector: string, timeout = 200) => {
  const el = await wait(until.elementLocated(By.css(selector)), timeout)
  return await wait(until.elementIsVisible(el), timeout)
}

export const $$ = async (selector: string, timeout = 200) => {
  return await wait(until.elementsLocated(By.css(selector)), timeout)
}

export const textEventuallyEquals = (
  selector: string,
  expected: string,
  timeout = 1000
) =>
  eventually(async () => {
    const element = await $(selector)
    expect(await element.getText()).toEqual(expected)
  }, timeout)

export const sleep = (time: number) =>
  new Promise((resolve) => setTimeout(resolve, time))

export const eventually = async (
  test: () => Promise<void>,
  timeout = 10000
) => {
  const expirationTime = new Date().getTime() + timeout
  let error = null
  while (true) {
    if (new Date().getTime() > expirationTime) {
      throw error
    }
    try {
      await test()
      return
    } catch (err) {
      error = err
      await sleep(timeout / 10)
    }
  }
}

export const deleteCookies = async () => {
  await driver.manage().deleteAllCookies()
}

export const reset = async (initialPath: string) => {
  await deleteCookies()
  await goToLocation(initialPath)
  await driver.wait(until.elementLocated(By.css("article#login-app")), 5000)
  await resetMockData()
}

export const resetMockData = async () => {
  await clickElement("#resetMockData")
  await textEventuallyEquals("#resetMockDataState", "success")
}

export const clearMockData = async () => {
  await clickElement("#clearMockData")
  await textEventuallyEquals("#clearMockDataState", "success")
}

export const loginAs = async (
  initialPath: string,
  username: string,
  password: string
) => {
  await reset(initialPath)
  ;(await $("#username")).sendKeys(username)
  ;(await $("#password")).sendKeys(password, Key.ENTER)
  await driver.wait(
    until.elementLocated(By.css("article.page:not(#login-app)")),
    5000
  )
  await driver.wait(until.elementLocated(By.css("article.page")), 5000)
}

export const defaultLogin = async (initialPath: string) =>
  loginAs(initialPath, "valpas-helsinki", "valpas-helsinki")

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

export const dataTableEventuallyEquals = async (
  selector: string,
  displayValues: string,
  timeout = 1000
) => {
  const expectedData = A.flatten(
    displayValues
      .split("\n")
      .map((row) => row.trim())
      .filter((row) => row.length > 0)
      .map((row) => row.split("\t"))
  )

  await eventually(async () => {
    const cells = await $$(`${selector} .table__body .table__td`)
    const actualData = (
      await Promise.all(cells.map((cell) => cell.getText()))
    ).map((value) => value.replace(/\n/g, ""))
    expect(actualData).toEqual(expectedData)
  }, timeout)
}

export const getCurrentUrl = () => driver.getCurrentUrl()

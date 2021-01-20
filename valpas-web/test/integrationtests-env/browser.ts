import {
  Builder,
  WebDriver,
  until,
  By,
  WebElementCondition,
  Key,
} from "selenium-webdriver"
import chrome from "selenium-webdriver/chrome"
import "chromedriver"

declare namespace global {
  let __driver__: undefined | (() => Promise<WebDriver>)
}

let driver: WebDriver
jasmine.DEFAULT_TIMEOUT_INTERVAL = 1000 * 60 * 5

beforeAll(async () => {
  driver = (await buildBrowserStackDriver()) || (await buildChromeDriver())
}, 20000)

afterAll(() => driver?.quit())

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

const wait = async (condition: WebElementCondition, timeout: number) => {
  return await driver.wait(async (d) => condition.fn(d), timeout)
}

export const goToLocation = async (path: string) => {
  await driver.get(`http://localhost:1234${path}`)
}

export const $ = async (selector: string, timeout = 200) => {
  const el = await wait(until.elementLocated(By.css(selector)), timeout)
  return await wait(until.elementIsVisible(el), timeout)
}

export const textEventuallyEquals = (selector: string, expected: string) =>
  eventually(async () => {
    const element = await $(selector)
    expect(await element.getText()).toEqual(expected)
  })

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
      await sleep(1000)
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

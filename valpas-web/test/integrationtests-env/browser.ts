import { Builder, WebDriver, until, By } from "selenium-webdriver"
import chrome from "selenium-webdriver/chrome"
import "chromedriver"

let driver: WebDriver
jasmine.DEFAULT_TIMEOUT_INTERVAL = 1000 * 60 * 5

beforeAll(async () => {
  const builder = new Builder().forBrowser("chrome")
  if (!process.env.SHOW_BROWSER) {
    builder.setChromeOptions(new chrome.Options().headless())
  }
  driver = await builder.build()
})

afterAll(async () => driver.quit())

// Helpers

export const goToLocation = async (path: string) => {
  await driver.get(`http://localhost:7357${path}`)
}

export const $ = async (selector: string, timeout = 20000) => {
  const el = await driver.wait(until.elementLocated(By.css(selector)), timeout)
  return await driver.wait(until.elementIsVisible(el), timeout)
}

export const textEquals = (selector: string, expected: string) =>
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

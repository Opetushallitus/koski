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
  console.log(`http://localhost:7357${path}`)
  await driver.get(`http://localhost:7357${path}`)
}

export const $ = async (selector: string, timeout = 20000) => {
  const el = await driver.wait(until.elementLocated(By.css(selector)), timeout)
  return await driver.wait(until.elementIsVisible(el), timeout)
}

export const getText = async (selector: string, timeout = 20000) => {
  const element = await $(selector, timeout)
  return element.getText()
}

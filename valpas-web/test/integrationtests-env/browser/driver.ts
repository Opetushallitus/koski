import "chromedriver"
import { Builder, WebDriver } from "selenium-webdriver"
import chrome from "selenium-webdriver/chrome"
import {
  expectCleanConsoleLogs,
  resetTestSpecificNetworkErrors,
} from "./fail-on-console"
import { clearLocalStorage, clearSessionStorage } from "./reset"

declare namespace global {
  let __driver__: undefined | (() => Promise<WebDriver>)
}

export let driver: WebDriver
jasmine.DEFAULT_TIMEOUT_INTERVAL = 1000 * 60 * 5

beforeAll(async () => {
  driver = (await buildBrowserStackDriver()) || (await buildChromeDriver())
}, 20000)

beforeEach(() => {
  resetTestSpecificNetworkErrors()
})

afterAll(() => driver?.quit())

afterEach(async () => {
  await expectCleanConsoleLogs(driver)

  // Storaget voi siivota vasta kun ollaan siirrytty johonkin osoitteeseen, joten niitä ei voi siivota vielä kun testiä alustetaan
  await clearLocalStorage()
  await clearSessionStorage()
})

const buildBrowserStackDriver = async (): Promise<WebDriver | undefined> =>
  // Browserstack webdriver is provided by jest-environment-browserstack
  // See jest.integrationtests.browserstack.config.js
  global.__driver__ && global.__driver__()

const buildChromeDriver = async (): Promise<WebDriver> => {
  const options = new chrome.Options().windowSize({ width: 1920, height: 1920 })
  if (!process.env.SHOW_BROWSER) {
    options.headless()
  }

  const builder = new Builder().forBrowser("chrome")
  builder.setChromeOptions(options)
  return builder.build()
}

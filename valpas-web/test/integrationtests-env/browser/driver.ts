import { afterAll, afterEach, beforeAll, beforeEach } from "@jest/globals"
import "chromedriver"
import { Builder, WebDriver } from "selenium-webdriver"
import chrome from "selenium-webdriver/chrome"
import { downloadDir } from "./downloads"
import {
  expectCleanConsoleLogs,
  resetTestSpecificNetworkErrors,
} from "./fail-on-console"
import { clearLocalStorage, clearSessionStorage } from "./reset"
import { longTimeout } from "./timeouts"

export let driver: WebDriver

beforeAll(async () => {
  driver = await buildChromeDriver()
}, longTimeout)

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

const buildChromeDriver = async (): Promise<WebDriver> => {
  const options = new chrome.Options()
    .windowSize({ width: 1920, height: 1920 })
    .setUserPreferences({
      "download.default_directory": downloadDir,
    })
  if (!process.env.SHOW_BROWSER) {
    options.addArguments("--headless=new")
  }

  const builder = new Builder().forBrowser("chrome")
  builder.setChromeOptions(options as any)
  return builder.build()
}

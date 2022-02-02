import { Builder } from "selenium-webdriver"
import chrome from "selenium-webdriver/chrome"

describe("Chromedriver on ajantasainen", () => {
  it(" (PÄIVITÄ valpas-web: npm i -D chromedriver@latest)", async () => {
    jest.setTimeout(30000)

    const options = new chrome.Options().headless()
    const builder = new Builder().forBrowser("chrome").setChromeOptions(options)
    const driver = await builder.build()

    expect(driver).toBeDefined()
  })
})

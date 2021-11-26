import { Builder } from "selenium-webdriver"
import chrome from "selenium-webdriver/chrome"

describe("Testiympäristön oikeellisuus", () => {
  it("Chromedriver on ajantasainen (päivitä uusin valpas-web -kansiossa: npm i -D chromedriver@latest)", async () => {
    jest.setTimeout(15000)

    const options = new chrome.Options().headless()
    const builder = new Builder().forBrowser("chrome").setChromeOptions(options)
    const driver = await builder.build()

    expect(driver).toBeDefined()
  })
})

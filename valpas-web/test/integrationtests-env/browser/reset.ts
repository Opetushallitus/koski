import { By, Key, until } from "selenium-webdriver"
import {
  clickElement,
  expectElementEventuallyVisible,
  textEventuallyEquals,
} from "./content"
import { $, deleteCookies, goToLocation } from "./core"
import { driver } from "./driver"
import { getTextInput, setTextInput } from "./forms"

export const loginAs = async (
  initialPath: string,
  username: string,
  password?: string
) => {
  await reset(initialPath)
  await expectElementEventuallyVisible("#username")
  ;(await $("#username")).sendKeys(username)
  ;(await $("#password")).sendKeys(password || username, Key.ENTER)
  await driver.wait(
    until.elementLocated(By.css("article.page:not(#login-app)")),
    5000
  )
  await driver.wait(until.elementLocated(By.css("article.page")), 5000)
}

export const defaultLogin = async (initialPath: string) =>
  loginAs(initialPath, "valpas-helsinki", "valpas-helsinki")

export const reset = async (initialPath: string) => {
  await deleteCookies()
  await goToLocation(initialPath)
  await driver.wait(until.elementLocated(By.css("article")), 5000)
  await resetMockData()
}

export const resetMockData = async (tarkastelupäivä: string = "2021-09-05") => {
  const inputSelector = "#tarkastelupäivä"

  const currentTarkastelupäivä = await getTextInput(inputSelector)
  const currentFixture = await (await $("#current-fixture")).getText()

  if (
    currentTarkastelupäivä !== tarkastelupäivä ||
    currentFixture !== "VALPAS"
  ) {
    await setTextInput(inputSelector, tarkastelupäivä)
    await clickElement("#resetMockData")
    await textEventuallyEquals("#resetMockDataState", "success", 30000)
  }
}

export const clearMockData = async () => {
  await clickElement("#clearMockData")
  await textEventuallyEquals("#clearMockDataState", "success", 30000)
}

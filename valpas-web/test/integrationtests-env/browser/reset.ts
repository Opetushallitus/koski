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
  forceReset: boolean = false
) => {
  await reset(initialPath, forceReset)
  await expectElementEventuallyVisible("#username")
  ;(await $("#username")).sendKeys(username)
  ;(await $("#password")).sendKeys(username, Key.ENTER)
  await driver.wait(
    until.elementLocated(By.css("article.page:not(#login-app)")),
    5000
  )
  await driver.wait(until.elementLocated(By.css("article.page")), 5000)
}

export const defaultLogin = async (initialPath: string) =>
  loginAs(initialPath, "valpas-helsinki")

export const reset = async (initialPath: string, force: boolean = false) => {
  await deleteCookies()
  await goToLocation(initialPath)
  await driver.wait(until.elementLocated(By.css("article")), 5000)
  await resetMockData(undefined, force)
}

export const resetMockData = async (
  tarkastelupäivä: string = "2021-09-05",
  force: boolean = false
) => {
  const inputSelector = "#tarkastelupäivä"

  const currentTarkastelupäivä = await getTextInput(inputSelector)
  const currentFixture = await (await $("#current-fixture")).getText()

  if (
    currentTarkastelupäivä !== tarkastelupäivä ||
    currentFixture !== "VALPAS" ||
    force
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

export const clearLocalStorage = async () => {
  await driver.executeScript("window.localStorage.clear()")
}

export const clearSessionStorage = async () => {
  await driver.executeScript("window.sessionStorage.clear()")
}

import { By, Key, until } from "selenium-webdriver"
import { expectElementEventuallyVisible } from "./content"
import { $, deleteCookies, goToLocation } from "./core"
import { driver } from "./driver"
import { allowNetworkError } from "./fail-on-console"
import { resetMockData } from "./reset"
import { defaultTimeout, longTimeout } from "./timeouts"
import { eventually } from "./utils"

const ignoreCalmmBaretErrors = () => {
  allowNetworkError("webpack-internal", "componentWillMount has been renamed")
  allowNetworkError(
    "webpack-internal",
    "componentWillReceiveProps has been renamed",
  )
}

export const resetKansalainen = async (
  initialPath: string,
  force: boolean = false,
  tarkastelupäivä?: string,
) => {
  ignoreCalmmBaretErrors()
  await deleteCookies()
  await goToLocation("")
  await driver.wait(until.elementLocated(By.css("article")), defaultTimeout)
  await resetMockData(tarkastelupäivä, force)
  await goToLocation(initialPath)
}

export const loginKansalainenAs = async (
  initialPath: string,
  hetu: string,
  forceReset: boolean = false,
  tarkastelupäivä?: string,
) => {
  await eventually(async () => {
    await resetKansalainen(initialPath, forceReset, tarkastelupäivä)
    await expectElementEventuallyVisible("#hetu")
  }, longTimeout)
  ;(await $("#hetu")).sendKeys(hetu, Key.ENTER)
  await driver.wait(
    until.elementLocated(By.css("article.kansalainenpage")),
    defaultTimeout,
  )
}

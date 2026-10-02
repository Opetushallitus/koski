import { By, until } from "selenium-webdriver"
import { deleteCookies, goToLocation } from "./core"
import { driver } from "./driver"
import { resetMockData } from "./reset"
import { defaultTimeout, longTimeout } from "./timeouts"
import { eventually } from "./utils"

export const resetKansalainen = async (
  force: boolean = false,
  tarkastelupäivä?: string,
) => {
  await deleteCookies()
  await goToLocation("")
  await driver.wait(until.elementLocated(By.css("article")), defaultTimeout)
  await resetMockData(tarkastelupäivä, force)
}

const mockLoginScript = `
  const [hetu, done] = arguments
  fetch("/koski/cas/oppija", {
    credentials: "include",
    redirect: "manual",
    headers: { hetu, security: "mock" },
  })
    .then(() => fetch("/koski/valpas/api/kansalainen/user", { credentials: "include" }))
    .then((response) => done(response.status), () => done(0))
`

// Kirjautuu samalla mock-kutsulla, jonka Kosken paikallinen kirjautumissivu tekisi.
// Itse kirjautumissivu testataan Kosken puolella (web/test/e2e/valpas-kansalaisen-kirjautuminen.spec.ts).
export const loginKansalainenAs = async (
  initialPath: string,
  hetu: string,
  forceReset: boolean = false,
  tarkastelupäivä?: string,
) => {
  await eventually(
    () => resetKansalainen(forceReset, tarkastelupäivä),
    longTimeout,
  )
  const userStatus = await driver.executeAsyncScript<number>(
    mockLoginScript,
    hetu,
  )
  expect(userStatus, `Kansalaisen ${hetu} kirjautuminen epäonnistui`).toBe(200)
  await goToLocation(initialPath)
  await driver.wait(
    until.elementLocated(By.css("article.kansalainenpage")),
    defaultTimeout,
  )
}

import { By, until } from "selenium-webdriver"
import { goToLocation } from "./core"
import { driver } from "./driver"
import { reset } from "./reset"
import { defaultTimeout, longTimeout } from "./timeouts"
import { eventually, withMessage } from "./utils"

const createKansalainenSession = (
  hetu: string,
  done: (userApiHttpStatus: number) => void,
) => {
  // Kirjautuu hetulla ja asettaa selaimeen istuntoevästeen
  fetch("/koski/cas/valpas/oppija", {
    // Estää palvelimen uudelleenohjauksen kirjautumisen jälkeiselle sivulle
    redirect: "manual",
    headers: { hetu, security: "mock" },
  })
    .then(() => fetch("/koski/valpas/api/kansalainen/user"))
    .then(
      (response) => done(response.status),
      () => done(0),
    )
}

export const loginKansalainenAs = async (
  initialPath: string,
  hetu: string,
  forceReset: boolean = false,
  tarkastelupäivä?: string,
) => {
  await eventually(async () => {
    await reset("", forceReset, tarkastelupäivä)
    const userApiHttpStatus = await driver.executeAsyncScript<number>(
      createKansalainenSession,
      hetu,
    )
    withMessage(`Kansalaisen ${hetu} kirjautuminen epäonnistui`, () =>
      expect(userApiHttpStatus).toBe(200),
    )
  }, longTimeout)
  await goToLocation(initialPath)
  await driver.wait(
    until.elementLocated(By.css("article.kansalainenpage")),
    defaultTimeout,
  )
}

import fetch from "node-fetch"
import { hakutilannePathWithOrg, oppijaPath } from "../../src/state/paths"
import {
  clickElement,
  expectElementEventuallyVisible,
  expectElementNotVisible,
  expectElementVisible,
} from "../integrationtests-env/browser/content"
import {
  pathToApiUrl,
  pathToUrl,
  urlIsEventually,
} from "../integrationtests-env/browser/core"
import { waitTableLoadingHasFinished } from "../integrationtests-env/browser/datatable"
import { allowNetworkError } from "../integrationtests-env/browser/fail-on-console"
import {
  defaultLogin,
  loginAs,
  reset,
} from "../integrationtests-env/browser/reset"
import { longTimeout } from "../integrationtests-env/browser/timeouts"

describe("Login / Logout / kirjautuminen", () => {
  it("Kirjautumattomalle käyttäjälle näytetään kirjautumisruutu, jossa ei näy logout-painiketta", async () => {
    await reset("/virkailija")
    await expectElementVisible("article.page#login-app")
    await expectElementNotVisible(".localraamit__logoutbutton")
  })

  it("Kirjautumisen jälkeen käyttäjä näkee varsinaisen sovelluksen ja logout-painikkeen", async () => {
    await defaultLogin("/virkailija")
    await expectElementVisible("#virkailija-app")
    await expectElementVisible(".localraamit__logoutbutton")
  })

  it("Kirjautuminen ei-Valpas-tunnuksilla näyttää virheen ja logout-painikkeen", async () => {
    await loginAs("/virkailija", "kalle")
    await expectElementVisible("article.page#error-view")
    await expectElementVisible(".localraamit__logoutbutton")
  })

  it("Kirjautunut käyttäjä palaa uloskirjautumisen jälkeen login-sivulle", async () => {
    await defaultLogin("/virkailija")
    await expectElementVisible(".localraamit__logoutbutton")
    await clickElement(".localraamit__logoutbutton")
    await expectElementEventuallyVisible("article.page#login-app")
  })

  it("Ei-Valpas-tunnuksilla kirjautunut käyttäjä palaa uloskirjautumisen jälkeen login-sivulle", async () => {
    await loginAs("/virkailija", "kalle")
    await expectElementVisible(".localraamit__logoutbutton")
    await clickElement(".localraamit__logoutbutton")
    await expectElementEventuallyVisible("article.page#login-app")
  })

  it("Käyttäjä on kirjautumisen jälkeen osoitteessa, jonne hän alunperin yritti", async () => {
    const path = oppijaPath.href("/virkailija", {
      oppijaOid: "1.2.246.562.24.00000000001",
    })
    await loginAs(path, "valpas-jkl-normaali")
    await urlIsEventually(pathToUrl(path))
  })

  it("Session vanheneminen vie käyttäjän kirjautumiseen", async () => {
    const organisaatioOid = "1.2.246.562.10.14613773812"
    await loginAs(
      hakutilannePathWithOrg.href("/virkailija", { organisaatioOid }),
      "valpas-jkl-normaali",
    )

    await waitTableLoadingHasFinished(".hakutilanne")

    // Salavihkainen logout (ei poista selaimesta keksiä)
    require("jest-fetch-mock").dontMock()
    await fetch(pathToApiUrl("/test/logout/valpas-jkl-normaali"))

    // Yritä selailla eteenpäin ja päädy kirjautumiseen
    allowNetworkError("/valpas/api/", "401 (Unauthorized)")

    const linkSelector = ".hakutilanne tbody tr td:first-child a"
    try {
      await clickElement(linkSelector)
    } catch (e) {
      // Ignore stale elements
    }

    await expectElementEventuallyVisible("article.page#login-app", longTimeout)
  })
})

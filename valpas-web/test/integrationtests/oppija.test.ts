import {
  contentEventuallyEquals,
  loginAs,
  textEventuallyEquals,
} from "../integrationtests-env/browser"
import {
  allowNetworkError,
  FORBIDDEN,
} from "../integrationtests-env/fail-on-console"

const mainHeadingEquals = (expected: string) =>
  textEventuallyEquals("h1.heading--primary", expected)
const secondaryHeadingEquals = (expected: string) =>
  textEventuallyEquals("h2.heading--secondary", expected)

describe("Oppijakohtainen näkymä", () => {
  it("Näyttää oppijan tiedot, johon käyttäjällä on lukuoikeus", async () => {
    await loginAs(
      "/oppijat/1.2.246.562.24.00000000001",
      "valpas-jkl-normaali",
      "valpas-jkl-normaali"
    )
    await mainHeadingEquals(
      "Oppivelvollinen-ysiluokka-kesken-keväällä-2021 Valpas (221105A3023)"
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000001")
    await contentEventuallyEquals(
      "#opiskeluhistoria .card__body",
      `
      school
      Perusopetus 2012 –
      Jyväskylän normaalikoulu
      Ryhmä: 9C
      `
    )
  })

  it("Ei näytä oppijan tietoja, johon käyttäjällä ei ole lukuoikeutta", async () => {
    allowNetworkError("/valpas/api/oppija/", FORBIDDEN)
    await loginAs(
      "/oppijat/1.2.246.562.24.00000000001",
      "valpas-helsinki",
      "valpas-helsinki"
    )
    await mainHeadingEquals("Oppijan tiedot")
    await secondaryHeadingEquals(
      "Oppijaa ei löydy tunnuksella 1.2.246.562.24.00000000001"
    )
  })

  it("Näyttää oppijalta, jolla on useampia opiskeluoikeuksia vain ne, johon käyttäjällä on lukuoikeus", async () => {
    await loginAs(
      "/oppijat/1.2.246.562.24.00000000003",
      "valpas-jkl-normaali",
      "valpas-jkl-normaali"
    )
    await mainHeadingEquals("Päällekkäisiä Oppivelvollisuuksia (060605A083N)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000003")
    await contentEventuallyEquals(
      "#opiskeluhistoria .card__body",
      `
      school
      Perusopetus 2012 –
      Jyväskylän normaalikoulu
      Ryhmä: 9B
      `
    )
  })
})

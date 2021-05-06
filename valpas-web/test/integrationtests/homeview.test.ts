import { textEventuallyEquals } from "../integrationtests-env/browser/content"
import {
  dataTableEventuallyEquals,
  dataTableHeadersEventuallyEquals,
} from "../integrationtests-env/browser/datatable"
import { loginAs } from "../integrationtests-env/browser/reset"

describe("Etusivun väliaikainen näkymä", () => {
  it("Näyttää ohjetekstin", async () => {
    await loginAs(
      "/virkailija",
      "valpas-maksuttomuus-hki",
      "valpas-maksuttomuus-hki"
    )

    await textEventuallyEquals(
      ".ohjeteksti",
      "Olet onnistuneesti kirjautunut Valpas-järjestelmään seuraavilla käyttöoikeuksilla"
    )
  })

  it("Näyttää käyttäjän käyttöoikeudet", async () => {
    await loginAs(
      "/virkailija",
      "valpas-maksuttomuus-hki",
      "valpas-maksuttomuus-hki"
    )

    await dataTableHeadersEventuallyEquals(
      ".kayttooikeudet",
      `
      Helsingin kaupunki
      Jyväskylän normaalikoulu
      `
    )
    await dataTableEventuallyEquals(
      ".kayttooikeudet",
      `
      Kunnan oppivelvollisuuden suorittamisen valvonta
      Oppilaitoksen opiskelun maksuttomuustietojen määrittely
      `
    )
  })

  it("Hakeutumisvelvollisuuden valvonnallinen käyttäjä ohjautuu hakeutumisvelvollisuusvalvonnan etusivulle", async () => {
    await loginAs("/virkailija", "valpas-jkl-normaali", "valpas-jkl-normaali")

    await textEventuallyEquals(
      ".card__header",
      "Hakeutumisvelvollisia oppijoita (16)",
      5000
    )
  })

  it("Pääkäyttäjä ohjautuu hakeutumisvelvollisuusvalvonnan etusivulle", async () => {
    await loginAs("/virkailija", "valpas-pää", "valpas-pää")

    await textEventuallyEquals(
      ".card__header",
      "Hakeutumisvelvollisia oppijoita (0)",
      5000
    )
  })

  it("Ei näytä käyttäjän Koski-käyttöoikeuksia", async () => {
    await loginAs(
      "/virkailija",
      "valpas-maksuttomuus-koski-hki",
      "valpas-maksuttomuus-koski-hki"
    )

    await dataTableHeadersEventuallyEquals(
      ".kayttooikeudet",
      `
      Jyväskylän normaalikoulu
      `
    )
    await dataTableEventuallyEquals(
      ".kayttooikeudet",
      `
      Oppilaitoksen opiskelun maksuttomuustietojen määrittely
      `
    )
  })
})

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
      "valpas-jkl-normaali-hki",
      "valpas-jkl-normaali-hki"
    )

    await textEventuallyEquals(
      ".ohjeteksti",
      "Olet onnistuneesti kirjautunut Valpas-järjestelmään seuraavilla käyttöoikeuksilla"
    )
  })

  it("Näyttää käyttäjän käyttöoikeudet", async () => {
    await loginAs(
      "/virkailija",
      "valpas-jkl-normaali-hki",
      "valpas-jkl-normaali-hki"
    )

    await dataTableHeadersEventuallyEquals(
      ".kayttooikeudet",
      `
      Helsingin kaupunki
      Jyväskylän normaalikoulu
      Jyväskylän normaalikoulu
      Jyväskylän normaalikoulu
      `
    )
    await dataTableEventuallyEquals(
      ".kayttooikeudet",
      `
      Kunnan oppivelvollisuuden suorittamisen valvonta
      Oppilaitoksen hakeutumisen valvonta
      Oppilaitoksen opiskelun maksuttomuustietojen määrittely
      Oppilaitoksen oppivelvollisuuden suorittamisen valvonta
      `
    )
  })

  it("Näyttää pääkäyttäjän käyttöoikeudet", async () => {
    await loginAs("/virkailija", "valpas-pää", "valpas-pää")

    await dataTableHeadersEventuallyEquals(
      ".kayttooikeudet",
      `
      Opetushallitus
      Opetushallitus
      Opetushallitus
      Opetushallitus
      `
    )
    await dataTableEventuallyEquals(
      ".kayttooikeudet",
      `
      Kunnan oppivelvollisuuden suorittamisen valvonta
      Oppilaitoksen hakeutumisen valvonta
      Oppilaitoksen opiskelun maksuttomuustietojen määrittely
      Oppilaitoksen oppivelvollisuuden suorittamisen valvonta
      `
    )
  })

  it("Ei näytä käyttäjän Koski-käyttöoikeuksia", async () => {
    await loginAs(
      "/virkailija",
      "valpas-jkl-normaali-koski-hki",
      "valpas-jkl-normaali-koski-hki"
    )

    await dataTableHeadersEventuallyEquals(
      ".kayttooikeudet",
      `
      Jyväskylän normaalikoulu
      Jyväskylän normaalikoulu
      Jyväskylän normaalikoulu
      `
    )
    await dataTableEventuallyEquals(
      ".kayttooikeudet",
      `
      Oppilaitoksen hakeutumisen valvonta
      Oppilaitoksen opiskelun maksuttomuustietojen määrittely
      Oppilaitoksen oppivelvollisuuden suorittamisen valvonta
      `
    )
  })
})

import {
  dataTableEventuallyEquals,
  dataTableHeadersEventuallyEquals,
  loginAs,
  reset,
  textEventuallyEquals,
} from "../integrationtests-env/browser"

describe("Etusivun väliaikainen näkymä", () => {
  it("Näyttää ohjetekstin", async () => {
    await reset("/")
    await loginAs("/", "valpas-jkl-normaali-hki", "valpas-jkl-normaali-hki")

    await textEventuallyEquals(
      ".ohjeteksti",
      "Olet onnistuneesti kirjautunut Valpas-järjestelmään seuraavilla käyttöoikeuksilla"
    )
  })

  it("Näyttää käyttäjän käyttöoikeudet", async () => {
    await reset("/")
    await loginAs("/", "valpas-jkl-normaali-hki", "valpas-jkl-normaali-hki")

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

  it("Ei näytä käyttäjän Koski-käyttöoikeuksia", async () => {
    await reset("/")
    await loginAs(
      "/",
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

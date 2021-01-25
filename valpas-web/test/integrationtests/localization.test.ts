import {
  clickElement,
  defaultLogin, expectElementVisible, reset,
  textEventuallyEquals,
} from "../integrationtests-env/browser"

describe("Lokalisointi", () => {
  it("Lokalisoitu otsikko ilmestyy", async () => {
    await defaultLogin("/")
    await textEventuallyEquals(
      ".heading--primary",
      "Valpas-komponenttikirjasto"
    )
  })

  it("Kieli vaihtuu yläpalkista", async () => {
    await reset("/")
    await expectElementVisible("article.page#login-app")

    await textEventuallyEquals(
      ".card__header",
      "Kirjautuminen"
    )

    await clickElement("#sv")

    await textEventuallyEquals(
      ".card__header",
      "Logga in"
    )

    await clickElement("#en")

    await textEventuallyEquals(
      ".card__header",
      "Login"
    )

    await clickElement("#fi")

    await textEventuallyEquals(
      ".card__header",
      "Kirjautuminen"
    )
  })
})

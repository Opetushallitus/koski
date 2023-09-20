import {
  clickElement,
  expectElementVisible,
  textEventuallyEquals,
} from "../integrationtests-env/browser/content"
import { reset } from "../integrationtests-env/browser/reset"

describe("Lokalisointi", () => {
  it("Lokalisoitu otsikko ilmestyy", async () => {
    await reset("/virkailija")
    await textEventuallyEquals(".card__header", "Kirjautuminen")
  })

  it("Kieli vaihtuu yläpalkista", async () => {
    await reset("/virkailija")
    await expectElementVisible("article.page#login-app")

    await textEventuallyEquals(".card__header", "Kirjautuminen")

    await clickElement("#sv")

    await textEventuallyEquals(".card__header", "Inloggning")

    await clickElement("#en")

    await textEventuallyEquals(".card__header", "Login")

    await clickElement("#fi")

    await textEventuallyEquals(".card__header", "Kirjautuminen")
  })
})

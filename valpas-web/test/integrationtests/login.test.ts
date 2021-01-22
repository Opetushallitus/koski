import {
  expectElementVisible,
  reset,
  loginAs,
  defaultLogin,
} from "../integrationtests-env/browser"

describe("Login / kirjautuminen", () => {
  it("Kirjautumattomalle käyttäjälle näytetään kirjautumisruutu", async () => {
    await reset("/")
    await expectElementVisible("article.page#login-app")
  })

  it("Kirjautumisen jälkeen käyttäjä näkee varsinaisen sovelluksen", async () => {
    await defaultLogin("/")
    await expectElementVisible("article.page#valpas-app")
  })

  it("Kirjautuminen ei-Valpas-tunnuksilla näyttää virheen", async () => {
    await loginAs("/", "kalle", "kalle")
    await expectElementVisible("article.page#error-view")
  })
})

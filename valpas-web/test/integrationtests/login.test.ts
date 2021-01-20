import {
  expectElementVisible,
  expectElementNotVisible,
  reset,
  loginAs,
} from "../integrationtests-env/browser"

describe("Login / kirjautuminen", () => {
  it("Kirjautumattomalle käyttäjälle näytetään kirjautumisruutu", async () => {
    await reset("/")
    await expectElementVisible("#login-app")
  })

  it("Kirjautumisen jälkeen käyttäjä näkee jonkin muun sivun", async () => {
    await loginAs("/", "kalle", "kalle")
    await expectElementNotVisible("#login-app")
  })
})

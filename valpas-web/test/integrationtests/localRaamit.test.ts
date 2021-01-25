import {
  expectElementVisible,
  reset,
  loginAs,
  defaultLogin,
} from "../integrationtests-env/browser"

describe("Paikalliset raamit", () => {
  it("näytetään login-sivulla kirjautumattomalle käyttäjälle", async () => {
    await reset("/")
    await expectElementVisible(".localraamit")
  })

  it("näytetään varsinaisessa sovelluksessa kirjautuneelle käyttäjälle", async () => {
    await defaultLogin("/")
    await expectElementVisible(".localraamit")
  })

  it("näytetään ilman Valpas-oikeuksia kirjautuneelle käyttäjlle virhesivulle", async () => {
    await loginAs("/", "kalle", "kalle")
    await expectElementVisible(".localraamit")
  })
})

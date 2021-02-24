import {
  defaultLogin,
  expectElementVisible,
  loginAs,
  reset,
} from "../integrationtests-env/browser"

describe("Paikalliset raamit", () => {
  it("näytetään login-sivulla kirjautumattomalle käyttäjälle", async () => {
    await reset("/virkailija")
    await expectElementVisible(".localraamit")
  })

  it("näytetään varsinaisessa sovelluksessa kirjautuneelle käyttäjälle", async () => {
    await defaultLogin("/virkailija")
    await expectElementVisible(".localraamit")
  })

  it("näytetään ilman Valpas-oikeuksia kirjautuneelle käyttäjlle virhesivulle", async () => {
    await loginAs("/virkailija", "kalle", "kalle")
    await expectElementVisible(".localraamit")
  })
})

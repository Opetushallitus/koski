import { expectElementVisible } from "../integrationtests-env/browser/content"
import {
  defaultLogin,
  loginAs,
  reset,
} from "../integrationtests-env/browser/reset"

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
    await loginAs("/virkailija", "kalle")
    await expectElementVisible(".localraamit")
  })
})

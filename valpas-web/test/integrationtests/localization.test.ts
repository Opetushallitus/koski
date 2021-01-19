import { loginAs, textEventuallyEquals } from "../integrationtests-env/browser"

describe("Lokalisointi", () => {
  it("Lokalisoitu otsikko ilmestyy", async () => {
    await loginAs("/", "kalle", "kalle")
    await textEventuallyEquals(".heading--primary", "Valpas-komponenttikirjasto")
  })
})

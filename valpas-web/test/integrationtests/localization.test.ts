import {
  defaultLogin,
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
})

import { getText, goToLocation } from "../integrationtests-env/browser"

describe("Esimerkkitesti", () => {
  beforeEach(async () => {
    await goToLocation("/")
  })

  it("Otsikko on 'Valpas-komponenttikirjasto'", async () => {
    expect(await getText(".heading--primary")).toEqual(
      "Valpas-komponenttikirjasto"
    )
  })
})

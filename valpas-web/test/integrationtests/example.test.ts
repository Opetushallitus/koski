import { goToLocation, textEquals } from "../integrationtests-env/browser"

describe("Esimerkkitesti", () => {
  beforeEach(async () => {
    await goToLocation("/")
  })

  it("Hello world -teksti ilmestyy", async () => {
    await textEquals("#helloworld", "Hello world!")
  })
})

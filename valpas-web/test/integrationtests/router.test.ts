import { textEventuallyEquals } from "../integrationtests-env/browser/content"
import { goToLocation } from "../integrationtests-env/browser/core"

describe("Reititys", () => {
  it("Näyttää virhesivun kun reittiä ei löydy", async () => {
    await goToLocation("/asdf")
    await textEventuallyEquals(".error-message", "Sivua ei löydy")
  })
})

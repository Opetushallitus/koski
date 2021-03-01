import {
  goToLocation,
  textEventuallyEquals,
} from "../integrationtests-env/browser"

describe("Reititys", () => {
  it("Näyttää virhesivun kun reittiä ei löydy", async () => {
    await goToLocation("/asdf")
    await textEventuallyEquals(".error-message", "Sivua ei löydy")
  })
})

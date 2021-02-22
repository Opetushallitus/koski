import {
  defaultLogin,
  textEventuallyEquals,
} from "../integrationtests-env/browser"

describe("Reititys", () => {
  it("Näyttää virhesivun kun reittiä ei löydy", async () => {
    await defaultLogin("/asdf")
    await textEventuallyEquals(".error-message", "Sivua ei löydy")
  })
})

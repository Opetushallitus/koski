import { loginAs, textEventuallyEquals } from "../integrationtests-env/browser"

describe("Esimerkkitesti", () => {
  it("Hello world -teksti ilmestyy", async () => {
    await loginAs("/", "kalle", "kalle")
    await textEventuallyEquals("#helloworld", "Hello world!")
  })
})

import {
  defaultLogin,
  textEventuallyEquals,
} from "../integrationtests-env/browser"

describe("Esimerkkitesti", () => {
  it("Hello world -teksti ilmestyy", async () => {
    await defaultLogin("/")
    await textEventuallyEquals("#helloworld", "Hello world!")
  })
})

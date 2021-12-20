import { kansalainenOmatTiedotPath } from "../../src/state/kansalainenPaths"
import { loginKansalainenAs } from "../integrationtests-env/browser/resetKansalainen"

const omatTiedotPath = kansalainenOmatTiedotPath.href()

describe("Kansalaisen näkymä", () => {
  it("Näyttää oppijan omat tiedot", async () => {
    await loginKansalainenAs(omatTiedotPath, "221105A3023")
  })
})

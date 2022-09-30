import {
  opo,
  teeKuntailmoitusHakutilannenäkymästä,
} from "./kuntailmoitus.shared"

describe("Kuntailmoituksen tekeminen 1/7", () => {
  it("happy path hakeutumisen valvojana", async () => {
    await teeKuntailmoitusHakutilannenäkymästä("valpas-jkl-normaali", opo)
  })
})

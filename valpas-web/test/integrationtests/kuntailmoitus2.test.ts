import {
  koulutustoimijaHakeutumisenValvoja,
  teeKuntailmoitusHakutilannenäkymästä,
} from "./kuntailmoitus.shared"

describe("Kuntailmoituksen tekeminen 2/7", () => {
  it("happy path hakeutumisen valvojana koulutustoimijana", async () => {
    await teeKuntailmoitusHakutilannenäkymästä(
      "valpas-jkl-yliopisto",
      koulutustoimijaHakeutumisenValvoja
    )
  })
})

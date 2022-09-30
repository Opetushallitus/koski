import {
  koulutustoimijaHakeutumisenValvoja,
  openHakutilanneView,
  teeKuntailmoitusOppijanäkymistä,
  teeOppijat,
} from "./kuntailmoitus.shared"

describe("Kuntailmoituksen tekeminen 4/7", () => {
  it("happy path oppijanäkymistä hakeutumisen valvojana koulutustoimijana", async () => {
    const oppijat = teeOppijat(koulutustoimijaHakeutumisenValvoja)

    await openHakutilanneView("valpas-jkl-yliopisto")

    await teeKuntailmoitusOppijanäkymistä(
      oppijat,
      koulutustoimijaHakeutumisenValvoja
    )
  })
})

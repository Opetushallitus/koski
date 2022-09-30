import {
  openHakutilanneView,
  opo,
  teeKuntailmoitusOppijanäkymistä,
  teeOppijat,
} from "./kuntailmoitus.shared"

describe("Kuntailmoituksen tekeminen 3/7", () => {
  it("happy path oppijanäkymistä hakeutumisen valvojana", async () => {
    const oppijat = teeOppijat(opo)

    await openHakutilanneView("valpas-jkl-normaali")

    await teeKuntailmoitusOppijanäkymistä(oppijat, opo)
  })
})

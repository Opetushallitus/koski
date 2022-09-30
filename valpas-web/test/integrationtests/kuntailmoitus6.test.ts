import { suorittaminenHetuhakuPath } from "../../src/state/paths"
import { loginAs } from "../integrationtests-env/browser/reset"
import {
  suorittamisenValvoja,
  suorittamisenValvojanOppijat,
  teeKuntailmoitusOppijanäkymistä,
} from "./kuntailmoitus.shared"

describe("Kuntailmoituksen tekeminen 6/7", () => {
  it("happy path suorittamisen valvojana", async () => {
    await loginAs(
      suorittaminenHetuhakuPath.href("/virkailija"),
      "valpas-pelkkä-suorittaminen",
      true
    )
    await teeKuntailmoitusOppijanäkymistä(
      suorittamisenValvojanOppijat,
      suorittamisenValvoja
    )
  })
})

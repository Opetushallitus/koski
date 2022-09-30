import { suorittaminenHetuhakuPath } from "../../src/state/paths"
import { loginAs } from "../integrationtests-env/browser/reset"
import {
  nivelvaiheenValvoja,
  nivelvaiheenValvojanOppijat,
  teeKuntailmoitusOppijanäkymistä,
} from "./kuntailmoitus.shared"

describe("Kuntailmoituksen tekeminen 7/7", () => {
  it("happy path hakeutumisen ja suorittamisen valvojana", async () => {
    await loginAs(
      suorittaminenHetuhakuPath.href("/virkailija"),
      "valpas-nivelvaihe",
      true
    )
    await teeKuntailmoitusOppijanäkymistä(
      nivelvaiheenValvojanOppijat,
      nivelvaiheenValvoja
    )
  })
})

import { dataTableEventuallyEquals } from "../integrationtests-env/browser/datatable"
import { loginAs } from "../integrationtests-env/browser/reset"
import { hakutilannePath } from "./hakutilanne.shared"
import {
  hkiTableContent_20211201,
  kunnanOppijat,
  kuntakäyttäjä,
  teeKuntailmoitusOppijanäkymistä,
} from "./kuntailmoitus.shared"

describe("Kuntailmoituksen tekeminen 5/7", () => {
  it("happy path kunnan käyttäjänä", async () => {
    await loginAs(hakutilannePath, "valpas-helsinki", true, "2021-12-01")
    await dataTableEventuallyEquals(
      ".kuntailmoitus",
      hkiTableContent_20211201,
      "|"
    )

    await teeKuntailmoitusOppijanäkymistä(kunnanOppijat, kuntakäyttäjä)
  })
})

import { loginAs } from "../integrationtests-env/browser/reset"
import {
  kunnalleIlmoitetutPathWithOrg,
  oppijaPath,
} from "../../src/state/paths"
import {
  clickElement,
  expectElementByTextEventuallyNotVisible,
  expectElementEventuallyNotVisible,
  expectElementEventuallyVisible,
  testId,
} from "../integrationtests-env/browser/content"
import { findElementByText } from "../integrationtests-env/browser/core"

const yksiIlmoitusOppijaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000036",
})

const jklNormaalikouluKunnalleIlmoitetutPath =
  kunnalleIlmoitetutPathWithOrg.href("/virkailija", {
    organisaatioOid: "1.2.246.562.10.14613773812",
  })

describe("Kuntailmoitusten mitätöinti", () => {
  it("Ilmoituksen voi mitätöidä oppijanäkymästä", async () => {
    await loginAs(
      yksiIlmoitusOppijaPath,
      "valpas-jkl-normaali",
      false,
      "2021-12-01",
    )
    await clickElement(testId("mitätöi-kuntailmoitus-btn"))
    await expectElementEventuallyVisible(testId("confirm-yes"))
    await clickElement(testId("confirm-yes"))
    // Oppijalla oli vain yksi ilmoitus joten se poistuu näkymästä
    await expectElementEventuallyVisible(".oppijaview__eiilmoituksia")
  })

  it("Ilmoituksen voi mitätöida tehdyt ilmoitukset -näkymästä", async () => {
    await loginAs(
      jklNormaalikouluKunnalleIlmoitetutPath,
      "valpas-jkl-normaali",
      false,
      "2021-12-05",
    )

    const rivi = await findElementByText("KahdenKoulunYsi-ilmo Valpas")
    await rivi.click()
    await clickElement(testId("mitätöi-kuntailmoitus-btn"))
    await expectElementEventuallyVisible(testId("confirm-yes"))
    await clickElement(testId("confirm-yes"))

    await expectElementByTextEventuallyNotVisible("KahdenKoulunYsi-ilmo Valpas")
  })

  it("Ilmoituksen vastaanottajalle ei näy mitätöinti-nappia", async () => {
    await loginAs(yksiIlmoitusOppijaPath, "valpas-pyhtää", false, "2021-12-01")
    await expectElementEventuallyVisible(".kuntailmoitus__frame")
    await expectElementEventuallyNotVisible(testId("mitätöi-kuntailmoitus-btn"))
  })
})

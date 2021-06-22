import { Oid } from "../../src/state/common"
import {
  createKuntailmoitusPath,
  createKuntailmoitusPathWithOrg,
  createOppijaPath,
} from "../../src/state/paths"
import {
  clickElement,
  expectElementEventuallyVisible,
  textEventuallyEquals,
} from "../integrationtests-env/browser/content"
import {
  pathToUrl,
  urlIsEventually,
} from "../integrationtests-env/browser/core"
import { dataTableEventuallyEquals } from "../integrationtests-env/browser/datatable"
import { dropdownSelect } from "../integrationtests-env/browser/forms"
import { loginAs, reset } from "../integrationtests-env/browser/reset"
import { helsinginKaupunkiOid, pyhtäänKuntaOid } from "./oids"

const openOppijaView = async (oppijaOid: Oid) => {
  const selector = `.kuntailmoitus .table__row td:first-child a[href*="${oppijaOid}"]`
  await expectElementEventuallyVisible(selector)
  await clickElement(selector)
}

const selectOrganisaatio = (index: number) =>
  dropdownSelect("#organisaatiovalitsin", index)

const rootPath = createKuntailmoitusPath("/virkailija")

describe("Kunnan listanäkymä", () => {
  beforeAll(async () => {
    await reset(rootPath, true)
  })

  it("Näyttää tyhjän listan virheittä, jos ei oppijoita", async () => {
    await loginAs(rootPath, "valpas-tornio")
    await urlIsEventually(pathToUrl(rootPath))
    await textEventuallyEquals(
      ".card__header",
      "Ilmoitetut oppivelvolliset ilman opiskelupaikkaa (0)"
    )
  })

  it("Ohjaa ensisijaiseen organisaatioon ja näyttää listan ilmoituksista", async () => {
    const expectedContent = `
      LukionAloittanutJaLopettanut-ilmo Valpas | 30.11.2021 | Jyväskylän normaalikoulu | 5.4.2005 | doneJyväskylän normaalikoulu, Lukiokoulutus
    `

    await loginAs(rootPath, "valpas-pyhtää-ja-helsinki")
    await urlIsEventually(
      pathToUrl(
        createKuntailmoitusPathWithOrg("/virkailija", helsinginKaupunkiOid)
      )
    )
    await textEventuallyEquals(
      ".card__header",
      "Ilmoitetut oppivelvolliset ilman opiskelupaikkaa (1)"
    )
    await dataTableEventuallyEquals(".kuntailmoitus", expectedContent, "|")
  })

  it("Vaihtaa taulun sisällön organisaatiovalitsimesta", async () => {
    await loginAs(rootPath, "valpas-pyhtää-ja-helsinki")

    await selectOrganisaatio(0)
    await urlIsEventually(
      pathToUrl(
        createKuntailmoitusPathWithOrg("/virkailija", helsinginKaupunkiOid)
      )
    )
    await textEventuallyEquals(
      ".card__header",
      "Ilmoitetut oppivelvolliset ilman opiskelupaikkaa (1)"
    )

    await selectOrganisaatio(1)
    await urlIsEventually(
      pathToUrl(createKuntailmoitusPathWithOrg("/virkailija", pyhtäänKuntaOid))
    )
    await textEventuallyEquals(
      ".card__header",
      "Ilmoitetut oppivelvolliset ilman opiskelupaikkaa (7)"
    )
  })

  it("Käyminen oppijakohtaisessa näkymässä ei hukkaa valittua organisaatiota", async () => {
    const pyhtäänOppijaOid = "1.2.246.562.24.00000000036"

    await loginAs(rootPath, "valpas-pyhtää-ja-helsinki")

    await selectOrganisaatio(1)
    await urlIsEventually(
      pathToUrl(createKuntailmoitusPathWithOrg("/virkailija", pyhtäänKuntaOid))
    )

    await openOppijaView(pyhtäänOppijaOid)
    await urlIsEventually(
      pathToUrl(
        createOppijaPath("/virkailija", {
          oppijaOid: pyhtäänOppijaOid,
          kuntailmoitusRef: pyhtäänKuntaOid,
        })
      )
    )

    await clickElement(".oppijaview__backbutton a")
    await urlIsEventually(
      createKuntailmoitusPathWithOrg("/virkailija", pyhtäänKuntaOid)
    )
  })
})

import { Oid } from "../../src/state/common"
import {
  kuntailmoitusPath,
  kuntailmoitusPathWithOrg,
  oppijaPath,
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
import { loginAs, reset } from "../integrationtests-env/browser/reset"
import {
  hkiTableContent_20211201,
  pyhtääTableContent,
  pyhtääTableContent_kaikkiIlmoitukset,
} from "./kuntailmoitus.shared"
import { helsinginKaupunkiOid, pyhtäänKuntaOid } from "./oids"
import {
  selectOrganisaatio,
  valitsimenOrganisaatiot,
} from "./organisaatiovalitsin-helpers"

const openOppijaView = async (oppijaOid: Oid) => {
  const selector = `.kuntailmoitus .table__row td:first-child a[href*="${oppijaOid}"]`
  await expectElementEventuallyVisible(selector)
  await clickElement(selector)
}

const rootPath = kuntailmoitusPath.href("/virkailija")

const ilmoitustitle = (
  näkyviäIlmoituksia: number,
  arkistoitujaIlmoituksia: number
) =>
  `Ilmoitetut oppivelvolliset ilman opiskelupaikkaa (${näkyviäIlmoituksia})Näytä aiemmin tehdyt ilmoitukset (${arkistoitujaIlmoituksia})`

describe("Kunnan listanäkymä", () => {
  beforeAll(async () => {
    await reset(rootPath, true)
  })

  it("Näyttää tyhjän listan virheittä, jos ei oppijoita", async () => {
    await loginAs(rootPath, "valpas-tornio")
    await urlIsEventually(pathToUrl(rootPath))
    await textEventuallyEquals(".card__header", ilmoitustitle(0, 0))
  })

  it("Ohjaa ensisijaiseen organisaatioon ja näyttää listan ilmoituksista", async () => {
    await loginAs(rootPath, "valpas-useita-kuntia", false, "2021-12-01")
    await urlIsEventually(
      pathToUrl(
        kuntailmoitusPathWithOrg.href("/virkailija", helsinginKaupunkiOid)
      )
    )
    await textEventuallyEquals(".card__header", ilmoitustitle(1, 2))
    await dataTableEventuallyEquals(
      ".kuntailmoitus",
      hkiTableContent_20211201,
      "|"
    )
  })

  it("Vaihtaa taulun sisällön organisaatiovalitsimesta", async () => {
    await loginAs(rootPath, "valpas-useita-kuntia")

    await selectOrganisaatio(0)
    await urlIsEventually(
      pathToUrl(
        kuntailmoitusPathWithOrg.href("/virkailija", helsinginKaupunkiOid)
      )
    )
    await textEventuallyEquals(".card__header", ilmoitustitle(1, 2))

    await selectOrganisaatio(1)
    await urlIsEventually(
      pathToUrl(kuntailmoitusPathWithOrg.href("/virkailija", pyhtäänKuntaOid))
    )
    await textEventuallyEquals(".card__header", ilmoitustitle(8, 5))
  })

  it("Käyminen oppijakohtaisessa näkymässä ei hukkaa valittua organisaatiota", async () => {
    const pyhtäänOppijaOid = "1.2.246.562.24.00000000036"

    await loginAs(rootPath, "valpas-useita-kuntia")

    await selectOrganisaatio(1)
    await urlIsEventually(
      pathToUrl(kuntailmoitusPathWithOrg.href("/virkailija", pyhtäänKuntaOid))
    )

    await openOppijaView(pyhtäänOppijaOid)
    await urlIsEventually(
      pathToUrl(
        oppijaPath.href("/virkailija", {
          oppijaOid: pyhtäänOppijaOid,
          kuntailmoitusRef: pyhtäänKuntaOid,
        })
      )
    )

    await clickElement(".oppijaview__backbutton a")
    await urlIsEventually(
      kuntailmoitusPathWithOrg.href("/virkailija", pyhtäänKuntaOid)
    )
  })

  it("Passiivisia organisaatioita ei listata", async () => {
    await loginAs(rootPath, "valpas-useita-kuntia")

    const organisaatiot = await valitsimenOrganisaatiot()

    const expectedOrganisaatiot = [
      "Helsingin kaupunki (1.2.246.562.10.346830761110)",
      "Pyhtään kunta (1.2.246.562.10.69417312936)",
    ]

    expect(organisaatiot).toEqual(expectedOrganisaatiot)
  })

  it("Valitsin näyttää ja piilottaa aiemmin tehdyt ilmoitukset", async () => {
    await loginAs(rootPath, "valpas-useita-kuntia")

    await selectOrganisaatio(1)
    await urlIsEventually(
      pathToUrl(kuntailmoitusPathWithOrg.href("/virkailija", pyhtäänKuntaOid))
    )

    await textEventuallyEquals(".card__header", ilmoitustitle(8, 5))
    await dataTableEventuallyEquals(".kuntailmoitus", pyhtääTableContent, "|")

    await clickElement('[data-testid="arkistoidutcb"]')

    await dataTableEventuallyEquals(
      ".kuntailmoitus",
      pyhtääTableContent_kaikkiIlmoitukset,
      "|"
    )
  })
})

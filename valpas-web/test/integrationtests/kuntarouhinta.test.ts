import { Oid } from "../../src/state/common"
import { kuntarouhintaPathWithOid } from "../../src/state/paths"
import {
  clickElement,
  expectElementEventuallyNotVisible,
  expectElementEventuallyVisible,
  textEventuallyEquals,
} from "../integrationtests-env/browser/content"
import { urlIsEventually } from "../integrationtests-env/browser/core"
import {
  dataTableEventuallyEquals,
  getExpectedRowCount,
} from "../integrationtests-env/browser/datatable"
import {
  cleanupDownloads,
  expectDownloadExists,
} from "../integrationtests-env/browser/downloads"
import { loginAs } from "../integrationtests-env/browser/reset"
import { pyhtäänKuntaOid } from "./oids"
import { selectOrganisaatioByNimi } from "./organisaatiovalitsin-helpers"

const pyhtäänKuntarouhintaPath = kuntarouhintaPathWithOid.href("/virkailija", {
  organisaatioOid: pyhtäänKuntaOid,
})

const pyhtäänKuntarouhintaTableContents = `
  Aikuisten-perusopetuksessa-aineopiskelija Valpas                        | 1.6.2004  | 1.2.246.562.24.00000000117  | 010604A727Y | Oppijalla ei ole oppivelvollisuuden suorittamiseen kelpaavaa opiskeluoikeutta | –           | –                     | –                                                             | –          | –      | –
  Amis-eronnut Valpas                                                     | 1.8.2005  | 1.2.246.562.24.00000000064  | 010805A852V | 2.9.2021                                                                      | Eronnut     | Ammatillinen tutkinto | Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka  | –          | –      | –
  Ei-opiskeluoikeuksia-oppivelvollisuusikäinen Valpas                     | 11.4.2005 | 1.2.246.562.24.00000000075  | 110405A6951 | Oppijalla ei ole oppivelvollisuuden suorittamiseen kelpaavaa opiskeluoikeutta | –           | –                     | –                                                             | –          | –      | –
  Ei-oppivelvollisuuden-suorittamiseen-kelpaavia-opiskeluoikeuksia Valpas | 6.10.2005 | 1.2.246.562.24.00000000058  | 061005A671V | Oppijalla ei ole oppivelvollisuuden suorittamiseen kelpaavaa opiskeluoikeutta | –           | –                     | –                                                             | –          | –      | –
  Eroaja-aiemmin Valpas                                                   | 24.9.2005 | 1.2.246.562.24.00000000008  | 240905A0078 | 1.1.2021                                                                      | Eronnut     | Perusopetus           | Jyväskylän normaalikoulu                                      | –          | –      | –
  Int-school-9-luokalta-valmistumisen-jälkeen-eronnut-aiemmin Valpas      | 17.4.2005 | 1.2.246.562.24.00000000091  | 170405A683H | 1.1.2021                                                                      | Eronnut     | International school  | International School of Helsinki                              | –          | –      | –
  Oppivelvollisuus-keskeytetty-ei-opiskele Valpas                         | 1.10.2005 | 1.2.246.562.24.00000000130  | 011005A115P | 15.5.2021                                                                     | Valmistunut | Perusopetus           | Jyväskylän normaalikoulu                                      | 16.8.2021– | Pyhtää | 20.5.2021
`

const vainOnrOppija = "1.2.246.562.24.00000000075"
const tavallinenOppija = "1.2.246.562.24.00000000058"

const pyhtäänKuntarouhintaCardHeader = `Pyhtää: Oppivelvolliset ilman opiskelupaikkaa (${getExpectedRowCount(
  pyhtäänKuntarouhintaTableContents
)})`

describe("Kuntarouhinta", () => {
  describe("Kuntarouhinnan tietojen lataaminen selaimeen", () => {
    it("Lataa oppivelvollisuutta suorittamattomat oppijat näkymään", async () => {
      await loginAs(pyhtäänKuntarouhintaPath, "valpas-monta")
      await urlIsEventually(pyhtäänKuntarouhintaPath)

      await confirmDataFetch()
      await expectPyhtääCardHeader()
      await expectPyhtääTableContents()

      await expectLinkExistsForOppija(tavallinenOppija)
      await expectNoLinkExistsForOppija(vainOnrOppija)
    })

    it("Tietojen lataamisen ja kunnan vaihtamisen jälkeen vahvistus lataamiselle kysytään uudelleen", async () => {
      await loginAs(pyhtäänKuntarouhintaPath, "valpas-monta")
      await urlIsEventually(pyhtäänKuntarouhintaPath)

      await confirmDataFetch()
      await expectPyhtääCardHeader()

      await selectOrganisaatioByNimi("Helsingin kaupunki")
      await expectDataFetchConfirmDialogVisible()

      await selectOrganisaatioByNimi("Pyhtään kunta")
      await expectPyhtääTableContents()
    })

    it("Toisessa näkymässä käynti ei hukkaa ladattuja datoja taulukosta", async () => {
      await loginAs(pyhtäänKuntarouhintaPath, "valpas-monta")
      await urlIsEventually(pyhtäänKuntarouhintaPath)

      await confirmDataFetch()
      await expectPyhtääTableContents()

      await clickNavTab(1)
      await clickNavTab(2)

      await expectPyhtääTableContents()
    })
  })

  describe("Kuntarouhinnan tietojen lataaminen tiedostona", () => {
    beforeAll(cleanupDownloads)

    it("Tietojen lataus vahvistusdialogista", async () => {
      await loginAs(pyhtäänKuntarouhintaPath, "valpas-monta")

      await expectDataFetchConfirmDialogVisible()
      await clickElement("#confirm-rouhinta-download-btn")

      await expectElementEventuallyVisible(
        ".kuntarouhintaview__confirmpassword"
      )
      await expectElementEventuallyNotVisible(".spinner")

      await expectDownloadExists("oppijahaku-kunta-2021-09-05.xlsx")
    })

    it("Tietojen lataus taulukosta", async () => {
      await loginAs(pyhtäänKuntarouhintaPath, "valpas-monta")
      await urlIsEventually(pyhtäänKuntarouhintaPath)

      await confirmDataFetch()
      await expectPyhtääCardHeader()

      await clickElement("#rouhinta-table-download-btn")

      await expectElementEventuallyVisible(".kuntarouhintaview__tablepassword")
      await expectElementEventuallyNotVisible(".spinner")

      await expectDownloadExists("oppijahaku-kunta-2021-09-05.xlsx")
    })
  })
})

const expectPyhtääCardHeader = () =>
  textEventuallyEquals(
    ".kuntarouhintaview__cardheaderlabel",
    pyhtäänKuntarouhintaCardHeader
  )

const expectPyhtääTableContents = () =>
  dataTableEventuallyEquals(
    ".kuntarouhintatable",
    pyhtäänKuntarouhintaTableContents,
    "|"
  )

const expectDataFetchConfirmDialogVisible = () =>
  expectElementEventuallyVisible("#rouhinta-fetch-confirm-dialog")

const confirmDataFetch = async () => {
  await expectDataFetchConfirmDialogVisible()
  await clickElement("#confirm-rouhinta-fetch-btn")
}

const clickNavTab = (index: number) =>
  clickElement(
    `.tabnavigation__list .tabnavigation__itemcontainer:nth-child(${index}) a`
  )

const oppijaRowSelector = (oppijaOid: Oid) =>
  `.kuntarouhintatable .table__row[data-row*="${oppijaOid}"] td:first-child a`

const expectLinkExistsForOppija = async (oppijaOid: Oid) => {
  const selector = oppijaRowSelector(oppijaOid)
  await expectElementEventuallyVisible(selector)
}

const expectNoLinkExistsForOppija = async (oppijaOid: Oid) => {
  const selector = oppijaRowSelector(oppijaOid)
  await expectElementEventuallyNotVisible(selector)
}

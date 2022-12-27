import {
  nivelvaiheenHakutilannePathWithOrg,
  nivelvaiheenHakutilannePathWithoutOrg,
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
import {
  dataTableEventuallyEquals,
  getTableContents,
  setTableTextFilter,
  toggleTableSort,
  waitTableLoadingHasFinished,
} from "../integrationtests-env/browser/datatable"
import { isCheckboxChecked } from "../integrationtests-env/browser/forms"
import { loginAs } from "../integrationtests-env/browser/reset"
import { eventually } from "../integrationtests-env/browser/utils"
import { openAnyOppijaView, openOppijaView } from "./hakutilanne.shared"
import {
  ressunLukioTableContent_lokakuu2021,
  ressunLukioTableContent_syyskuu2021,
  ressunLukioTableHead_lokakuu2021,
  ressunLukioTableHead_syyskuu2021,
} from "./nivelvaihehakutilanne.shared"
import {
  aapajoenKouluOid,
  internationalSchoolOid,
  ressunLukioOid,
  saksalainenKouluOid,
} from "./oids"
import { selectOrganisaatioByNimi } from "./organisaatiovalitsin-helpers"

const nivelvaiheenHakutilannePath =
  nivelvaiheenHakutilannePathWithoutOrg.href("/virkailija")

const ressunLukioHakutilannePath = nivelvaiheenHakutilannePathWithOrg.href(
  "/virkailija",
  {
    organisaatioOid: ressunLukioOid,
  }
)

const aapajoenKouluHakutilannePath = nivelvaiheenHakutilannePathWithOrg.href(
  "/virkailija",
  {
    organisaatioOid: aapajoenKouluOid,
  }
)

const internationalSchoolHakutilannePath =
  nivelvaiheenHakutilannePathWithOrg.href("/virkailija", {
    organisaatioOid: internationalSchoolOid,
  })

const saksalainenKouluHakutilannePath = nivelvaiheenHakutilannePathWithOrg.href(
  "/virkailija",
  { organisaatioOid: saksalainenKouluOid }
)

describe("Nivelvaiheen hakutilannenäkymä", () => {
  it("Näyttää listan oppijoista, syyskuu 2021", async () => {
    await loginAs(ressunLukioHakutilannePath, "valpas-monta")
    await urlIsEventually(pathToUrl(ressunLukioHakutilannePath))

    await textEventuallyEquals(
      ".card__header",
      ressunLukioTableHead_syyskuu2021
    )

    await dataTableEventuallyEquals(
      ".hakutilanne",
      ressunLukioTableContent_syyskuu2021,
      "|"
    )
  })

  it("Näyttää listan oppijoista, lokakuu 2021", async () => {
    await loginAs(
      ressunLukioHakutilannePath,
      "valpas-monta",
      false,
      "2021-10-01"
    )
    await urlIsEventually(pathToUrl(ressunLukioHakutilannePath))

    await textEventuallyEquals(
      ".card__header",
      ressunLukioTableHead_lokakuu2021
    )

    await dataTableEventuallyEquals(
      ".hakutilanne",
      ressunLukioTableContent_lokakuu2021,
      "|"
    )
  })

  it("Näyttää tyhjän listan virheittä, jos ei oppijoita x", async () => {
    await loginAs(nivelvaiheenHakutilannePath, "valpas-monta")
    await urlIsEventually(pathToUrl(saksalainenKouluHakutilannePath))
    await textEventuallyEquals(
      ".card__header",
      "Hakeutumisvelvollisia oppijoita (0)"
    )

    await waitTableLoadingHasFinished(".hakutilanne")
  })

  it("Vaihtaa taulun sisällön organisaatiovalitsimesta", async () => {
    await loginAs(nivelvaiheenHakutilannePath, "valpas-monta")

    await selectOrganisaatioByNimi(ressunLukioOid)
    await urlIsEventually(pathToUrl(ressunLukioHakutilannePath))
    await textEventuallyEquals(
      ".card__header",
      ressunLukioTableHead_syyskuu2021
    )
    await dataTableEventuallyEquals(
      ".hakutilanne",
      ressunLukioTableContent_syyskuu2021,
      "|"
    )

    await selectOrganisaatioByNimi(internationalSchoolOid)
    await urlIsEventually(pathToUrl(internationalSchoolHakutilannePath))
    await textEventuallyEquals(
      ".card__header",
      "Hakeutumisvelvollisia oppijoita (0)"
    )
    await waitTableLoadingHasFinished(".hakutilanne")
  })

  it("Toimii koulutustoimijatason käyttäjällä", async () => {
    await loginAs(nivelvaiheenHakutilannePath, "valpas-helsinki-peruskoulu")

    await selectOrganisaatioByNimi(ressunLukioOid)
    await urlIsEventually(pathToUrl(ressunLukioHakutilannePath))
    await textEventuallyEquals(
      ".card__header",
      "Hakeutumisvelvollisia oppijoita (4)"
    )

    await dataTableEventuallyEquals(
      ".hakutilanne",
      ressunLukioTableContent_syyskuu2021,
      "|"
    )
  })

  it("Toimii passivoidun organisaation käyttäjällä", async () => {
    await loginAs(nivelvaiheenHakutilannePath, "valpas-aapajoen-koulu")

    await selectOrganisaatioByNimi("LAKKAUTETTU: Aapajoen koulu")
    await urlIsEventually(pathToUrl(aapajoenKouluHakutilannePath))
    await textEventuallyEquals(
      ".card__header",
      "Hakeutumisvelvollisia oppijoita (0)"
    )
    await waitTableLoadingHasFinished(".hakutilanne")
  })

  it("Käyminen oppijakohtaisessa näkymässä ei hukkaa valittua organisaatiota", async () => {
    await loginAs(nivelvaiheenHakutilannePath, "valpas-monta")

    await selectOrganisaatioByNimi(ressunLukioOid)
    await urlIsEventually(pathToUrl(ressunLukioHakutilannePath))

    const oppijaOid = "1.2.246.562.24.00000000114"
    await openOppijaView(oppijaOid)
    await urlIsEventually(
      pathToUrl(
        oppijaPath.href("/virkailija", {
          oppijaOid: oppijaOid,
          hakutilanneNivelvaiheRef: ressunLukioOid,
        })
      )
    )

    await clickElement(".oppijaview__backbutton a")
    await urlIsEventually(pathToUrl(ressunLukioHakutilannePath))
    await waitTableLoadingHasFinished(".hakutilanne")
  })

  it("Käyminen oppijakohtaisessa näkymässä ei hukkaa filttereiden tai järjestyksen tilaa", async () => {
    await loginAs(ressunLukioHakutilannePath, "valpas-monta")
    await urlIsEventually(pathToUrl(ressunLukioHakutilannePath))

    // Vaihda filtteriä ja järjestyksen suuntaa nimen perusteella
    const selector = ".hakutilanne"
    await setTableTextFilter(selector, 1, "valmi")
    await toggleTableSort(selector, 1)

    // Ota snapshot talteen taulukon tilasta
    await waitTableLoadingHasFinished(".hakutilanne")
    const contentsBefore = await getTableContents(selector)

    // Käy jossakin oppijanäkymässä
    await openAnyOppijaView()
    await expectElementEventuallyVisible(".oppijaview__backbutton a")
    await clickElement(".oppijaview__backbutton a")

    // Taulukon tilan pitäisi olla sama kuin aiemmin
    await urlIsEventually(pathToUrl(ressunLukioHakutilannePath))
    await waitTableLoadingHasFinished(".hakutilanne")
    const contentsAfter = await getTableContents(selector)
    expect(contentsAfter).toEqual(contentsBefore)
  })

  it("Muu haku -täppä toimii ja tallentuu", async () => {
    const loadPage = async () => {
      await loginAs(ressunLukioHakutilannePath, "valpas-monta")
      await urlIsEventually(pathToUrl(ressunLukioHakutilannePath))
      await textEventuallyEquals(
        ".card__header",
        ressunLukioTableHead_syyskuu2021
      )
    }

    await waitTableLoadingHasFinished(".hakutilanne")
    const getState = () => Promise.all([0, 1, 2, 3].map(isMuuHakuChecked))

    await loadPage()
    for (const rowIndex of [1, 3, 2, 1, 1]) {
      await clickAndVerifyMuuHaku(rowIndex)
    }

    const stateBeforeReload = await getState()
    await loadPage()
    const stateAfterReload = await getState()

    expect(stateAfterReload).toEqual(stateBeforeReload)
  })

  it("Organisaation vaihtaminen muistaa muu haku -valinnat", async () => {
    await loginAs(ressunLukioHakutilannePath, "valpas-monta")
    await urlIsEventually(pathToUrl(ressunLukioHakutilannePath))
    await textEventuallyEquals(
      ".card__header",
      ressunLukioTableHead_syyskuu2021
    )

    const getState = () => Promise.all([0, 1, 2, 3].map(isMuuHakuChecked))

    for (const rowIndex of [1, 2, 3, 1]) {
      await clickAndVerifyMuuHaku(rowIndex)
    }

    const stateBeforeOrgChange = await getState()
    await selectOrganisaatioByNimi(internationalSchoolOid)
    await textEventuallyEquals(
      ".card__header",
      "Hakeutumisvelvollisia oppijoita (0)"
    )

    await selectOrganisaatioByNimi(ressunLukioOid)
    await textEventuallyEquals(
      ".card__header",
      ressunLukioTableHead_syyskuu2021
    )

    const stateAfterOrgChange = await getState()

    await waitTableLoadingHasFinished(".hakutilanne")
    expect(stateBeforeOrgChange).toEqual(stateAfterOrgChange)
  })
})

const clickAndVerifyMuuHaku = async (index: number) => {
  const currentState = await isMuuHakuChecked(index)
  await clickElement(
    `.hakutilanne tr:nth-child(${
      index + 1
    }) td:last-child .toggleswitch__container`
  )
  await eventually(async () =>
    expect(await isMuuHakuChecked(index)).toBe(!currentState)
  )
}
const isMuuHakuChecked = (index: number) =>
  isCheckboxChecked(
    `.hakutilanne tr:nth-child(${index + 1}) td:last-child input`
  )

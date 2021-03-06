import {
  createHakutilannePathWithOrg,
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
import {
  dataTableEventuallyEquals,
  getTableContents,
  setTableTextFilter,
  toggleTableSort,
} from "../integrationtests-env/browser/datatable"
import {
  dropdownSelect,
  dropdownSelectAllOptionTexts,
  dropdownSelectContains,
  isCheckboxChecked,
} from "../integrationtests-env/browser/forms"
import { loginAs } from "../integrationtests-env/browser/reset"
import { eventually } from "../integrationtests-env/browser/utils"
import {
  hakutilannePath,
  jklNormaalikouluTableContent,
  jklNormaalikouluTableHead,
  openAnyOppijaView,
  openOppijaView,
} from "./hakutilanne.shared"
import {
  aapajoenKouluOid,
  jyväskylänNormaalikouluOid,
  kulosaarenAlaAsteOid,
} from "./oids"

const selectOrganisaatio = (index: number) =>
  dropdownSelect("#organisaatiovalitsin", index)
const selectOrganisaatioByNimi = (text: string) =>
  dropdownSelectContains("#organisaatiovalitsin", text)
const valitsimenOrganisaatiot = () =>
  dropdownSelectAllOptionTexts("#organisaatiovalitsin")

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

const kulosaarenAlaAsteTableContent = `
  Jkl-Esikoulu-Kulosaarelainen Valpas                     | 22.3.2004   | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
  Jkl-Lukio-Kulosaarelainen Valpas                        | 1.1.2004    | 9C | –          | Ei hakemusta         | –                           | –                         | doneJyväskylän normaalikoulu, Lukiokoulutus                                |
  Jkl-Nivel-Kulosaarelainen Valpas                        | 1.1.2004    | 9C | –          | Ei hakemusta         | –                           | –                         | doneJyväskylän normaalikoulu, Perusopetuksen lisäopetus                    |
  Kulosaarelainen Oppija                                  | 19.1.2005   | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
`

const aapajaoenKouluTableContent = `
  Aaapajoen-peruskoulusta-valmistunut Valpas              | 16.2.2005   | 9C | 29.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
  Kahdella-oppija-oidilla Valpas                          | 15.2.2005   | 9C | 29.5.2021  | Hakenut open_in_new  | Varasija: Ressun lukio      | –                         | doneJyväskylän normaalikoulu, Lukiokoulutus                                |
  Kahdella-oppija-oidilla-ilmo Valpas                     | 4.6.2005    | 9C | 29.5.2021  | Ei hakemusta         | –                           | –                         | doneJyväskylän normaalikoulu, Lukiokoulutus                                |
  KahdenKoulunYsi-ilmo Valpas                             | 21.11.2004  | 9C | 29.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
`

const jklHakutilannePath = createHakutilannePathWithOrg("/virkailija", {
  organisaatioOid: jyväskylänNormaalikouluOid,
})
const kulosaariHakutilannePath = createHakutilannePathWithOrg("/virkailija", {
  organisaatioOid: kulosaarenAlaAsteOid,
})
const aapajoenKouluHakutilannePath = createHakutilannePathWithOrg(
  "/virkailija",
  {
    organisaatioOid: aapajoenKouluOid,
  }
)

const kulosaarenOppijaOid = "1.2.246.562.24.00000000029"
const saksalainenKouluOid = "1.2.246.562.10.45093614456"
const saksalainenKouluHakutilannePath = createHakutilannePathWithOrg(
  "/virkailija",
  {
    organisaatioOid: saksalainenKouluOid,
  }
)

describe("Hakutilannenäkymä", () => {
  it("Näyttää listan oppijoista", async () => {
    await loginAs(hakutilannePath, "valpas-jkl-normaali")
    await urlIsEventually(pathToUrl(jklHakutilannePath))
    await textEventuallyEquals(".card__header", jklNormaalikouluTableHead)
    await dataTableEventuallyEquals(
      ".hakutilanne",
      jklNormaalikouluTableContent,
      "|"
    )
  })

  it("Näyttää tyhjän listan virheittä, jos ei oppijoita", async () => {
    await loginAs(hakutilannePath, "valpas-saksalainen")
    await urlIsEventually(pathToUrl(saksalainenKouluHakutilannePath))
    await textEventuallyEquals(
      ".card__header",
      "Hakeutumisvelvollisia oppijoita (0)"
    )
  })

  it("Vaihtaa taulun sisällön organisaatiovalitsimesta", async () => {
    await loginAs(hakutilannePath, "valpas-useampi-peruskoulu")

    await selectOrganisaatio(0)
    await urlIsEventually(pathToUrl(jklHakutilannePath))
    await textEventuallyEquals(".card__header", jklNormaalikouluTableHead)
    await dataTableEventuallyEquals(
      ".hakutilanne",
      jklNormaalikouluTableContent,
      "|"
    )

    await selectOrganisaatio(1)
    await urlIsEventually(pathToUrl(kulosaariHakutilannePath))
    await textEventuallyEquals(
      ".card__header",
      "Hakeutumisvelvollisia oppijoita (4)"
    )
  })

  it("Toimii koulutustoimijatason käyttäjällä", async () => {
    await loginAs(hakutilannePath, "valpas-helsinki-peruskoulu")

    await selectOrganisaatioByNimi("Kulosaaren ala-aste")
    await urlIsEventually(pathToUrl(kulosaariHakutilannePath))
    await textEventuallyEquals(
      ".card__header",
      "Hakeutumisvelvollisia oppijoita (4)"
    )

    await dataTableEventuallyEquals(
      ".hakutilanne",
      kulosaarenAlaAsteTableContent,
      "|"
    )
  })

  it("Passiiviset organisaatiot listataan aktiivisten jälkeen", async () => {
    await loginAs(hakutilannePath, "valpas-aapajoen-koulu-jkl-normaali")

    const organisaatiot = await valitsimenOrganisaatiot()

    const expectedOrganisaatiot = [
      "Jyväskylän normaalikoulu (1.2.246.562.10.14613773812)",
      "LAKKAUTETTU: Aapajoen koulu (1.2.246.562.10.26197302388)",
    ]

    expect(organisaatiot).toEqual(expectedOrganisaatiot)
  })

  it("Toimii passivoidun organisaation käyttäjällä", async () => {
    await loginAs(hakutilannePath, "valpas-aapajoen-koulu")

    await selectOrganisaatioByNimi("LAKKAUTETTU: Aapajoen koulu")
    await urlIsEventually(pathToUrl(aapajoenKouluHakutilannePath))
    await textEventuallyEquals(
      ".card__header",
      "Hakeutumisvelvollisia oppijoita (4)"
    )

    await dataTableEventuallyEquals(
      ".hakutilanne",
      aapajaoenKouluTableContent,
      "|"
    )
  })

  it("Käyminen oppijakohtaisessa näkymässä ei hukkaa valittua organisaatiota", async () => {
    await loginAs(hakutilannePath, "valpas-useampi-peruskoulu")

    await selectOrganisaatio(1)
    await urlIsEventually(pathToUrl(kulosaariHakutilannePath))

    await openOppijaView(kulosaarenOppijaOid)
    await urlIsEventually(
      pathToUrl(
        createOppijaPath("/virkailija", {
          oppijaOid: kulosaarenOppijaOid,
          hakutilanneRef: kulosaarenAlaAsteOid,
        })
      )
    )

    await clickElement(".oppijaview__backbutton a")
    await urlIsEventually(pathToUrl(kulosaariHakutilannePath))
  })

  it("Käyminen oppijakohtaisessa näkymässä ei hukkaa filttereiden tai järjestyksen tilaa", async () => {
    await loginAs(hakutilannePath, "valpas-jkl-normaali")

    // Vaihda filtteriä ja järjestyksen suuntaa nimen perusteella
    const selector = ".hakutilanne"
    await setTableTextFilter(selector, 1, "luoka")
    await toggleTableSort(selector, 1)

    // Ota snapshot talteen taulukon tilasta
    const contentsBefore = await getTableContents(selector)

    // Käy jossakin oppijanäkymässä
    await openAnyOppijaView()
    await expectElementEventuallyVisible(".oppijaview__backbutton a")
    await clickElement(".oppijaview__backbutton a")

    // Taulukon tilan pitäisi olla sama kuin aiemmin
    await urlIsEventually(pathToUrl(jklHakutilannePath))
    const contentsAfter = await getTableContents(selector)
    expect(contentsAfter).toEqual(contentsBefore)
  })

  it("Oppijasivulta, jolta puuttuu organisaatioreferenssi, ohjataan oikean organisaation hakutilannenäkymään", async () => {
    await loginAs(
      createOppijaPath("/virkailija", {
        oppijaOid: kulosaarenOppijaOid,
      }),
      "valpas-useampi-peruskoulu"
    )

    await clickElement(".oppijaview__backbutton a")
    await urlIsEventually(pathToUrl(kulosaariHakutilannePath), 5000)
  })

  it("Muu haku -täppä toimii ja tallentuu", async () => {
    const loadPage = async () => {
      await loginAs(hakutilannePath, "valpas-jkl-normaali")
      await urlIsEventually(pathToUrl(jklHakutilannePath))
      await textEventuallyEquals(".card__header", jklNormaalikouluTableHead)
    }

    const getState = () => Promise.all([1, 2, 3, 4].map(isMuuHakuChecked))

    await loadPage()
    for (const rowIndex of [1, 3, 4, 3, 1, 1]) {
      await clickAndVerifyMuuHaku(rowIndex)
    }

    const stateBeforeReload = await getState()
    await loadPage()
    const stateAfterReload = await getState()

    expect(stateAfterReload).toEqual(stateBeforeReload)
  })

  it("Organisaation vaihtaminen muistaa muu haku -valinnat", async () => {
    await loginAs(hakutilannePath, "valpas-useampi-peruskoulu")
    await urlIsEventually(pathToUrl(jklHakutilannePath))
    await textEventuallyEquals(".card__header", jklNormaalikouluTableHead)

    const getState = () => Promise.all([1, 2, 3, 4].map(isMuuHakuChecked))

    for (const rowIndex of [1, 3, 4, 3, 1, 1]) {
      await clickAndVerifyMuuHaku(rowIndex)
    }

    const stateBeforeOrgChange = await getState()
    await selectOrganisaatio(1)
    await textEventuallyEquals(
      ".card__header",
      "Hakeutumisvelvollisia oppijoita (4)"
    )

    await selectOrganisaatio(0)
    await textEventuallyEquals(".card__header", jklNormaalikouluTableHead)

    const stateAfterOrgChange = await getState()

    expect(stateBeforeOrgChange).toEqual(stateAfterOrgChange)
  })
})

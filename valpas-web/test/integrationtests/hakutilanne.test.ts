import { hakutilannePathWithOrg, oppijaPath } from "../../src/state/paths"
import {
  clickElement,
  expectElementEventuallyVisible,
  expectLinkToEqual,
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
import {
  europeanSchoolOfHelsinkiTableContent,
  europeanSchoolOfHelsinkiTableContentLater,
  europeanSchoolOfHelsinkiTableHead,
  europeanSchoolOfHelsinkiTableHeadLater,
  hakutilannePath,
  internationalSchoolTableContent,
  internationalSchoolTableHead,
  jklNormaalikouluTableContent,
  jklNormaalikouluTableHead,
  openAnyOppijaView,
  openOppijaView,
} from "./hakutilanne.shared"
import {
  aapajoenKouluOid,
  europeanSchoolOfHelsinkiOid,
  internationalSchoolOid,
  jyväskylänNormaalikouluOid,
  kulosaarenAlaAsteOid,
  saksalainenKouluOid,
} from "./oids"
import {
  selectOrganisaatio,
  selectOrganisaatioByNimi,
  valitsimenOrganisaatiot,
} from "./organisaatiovalitsin-helpers"

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
  Oppivelvollinen-hetullinen Valpas                       | 3.1.2005    | 9C | –          | Ei hakemusta         | –                           | –                         | –                                                                          |
`

const aapajaoenKouluTableContent = `
  Aaapajoen-peruskoulusta-valmistunut Valpas              | 16.2.2005   | 9C | 29.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
  Amis-ja-YO Valpas                                       | 30.8.2005   | 9C | 29.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
  Kahdella-oppija-oidilla Valpas                          | 15.2.2005   | 9C | 29.5.2021  | Hakenut open_in_new  | Varasija: Ressun lukio      | –                         | doneJyväskylän normaalikoulu, Lukiokoulutus                                |
  Kahdella-oppija-oidilla-ilmo Valpas                     | 4.6.2005    | 9C | 29.5.2021  | Ei hakemusta         | –                           | –                         | doneJyväskylän normaalikoulu, Lukiokoulutus                                |
  KahdenKoulunYsi-ilmo Valpas                             | 21.11.2004  | 9C | 29.5.2021  | Ei hakemusta         | –                           | –                         | –                                                                          |
`

const jklHakutilannePath = hakutilannePathWithOrg.href("/virkailija", {
  organisaatioOid: jyväskylänNormaalikouluOid,
})
const kulosaariHakutilannePath = hakutilannePathWithOrg.href("/virkailija", {
  organisaatioOid: kulosaarenAlaAsteOid,
})
const aapajoenKouluHakutilannePath = hakutilannePathWithOrg.href(
  "/virkailija",
  {
    organisaatioOid: aapajoenKouluOid,
  }
)

const internationalSchoolHakutilannePath = hakutilannePathWithOrg.href(
  "/virkailija",
  {
    organisaatioOid: internationalSchoolOid,
  }
)

const europeanSchoolOfHelsinkiHakutilannePath = hakutilannePathWithOrg.href(
  "/virkailija",
  {
    organisaatioOid: europeanSchoolOfHelsinkiOid,
  }
)

const saksalainenKouluHakutilannePath = hakutilannePathWithOrg.href(
  "/virkailija",
  {
    organisaatioOid: saksalainenKouluOid,
  }
)

const kulosaarenOppijaOid = "1.2.246.562.24.00000000029"
const viikinNormaalikouluOid = "1.2.246.562.10.81927839589"

const viikinNormaalikouluHakutilannePath = hakutilannePathWithOrg.href(
  "/virkailija",
  {
    organisaatioOid: viikinNormaalikouluOid,
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
    await loginAs(hakutilannePath, "valpas-viikin-normaalikoulu")
    await urlIsEventually(pathToUrl(viikinNormaalikouluHakutilannePath))
    await textEventuallyEquals(".card__header", "Hakeutumisvelvollisia (0)")

    await waitTableLoadingHasFinished(".hakutilanne")
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
    await textEventuallyEquals(".card__header", "Hakeutumisvelvollisia (5)")

    await waitTableLoadingHasFinished(".hakutilanne")
  })

  it("Toimii koulutustoimijatason käyttäjällä", async () => {
    await loginAs(hakutilannePath, "valpas-helsinki-peruskoulu")

    await selectOrganisaatioByNimi("Kulosaaren ala-aste")
    await urlIsEventually(pathToUrl(kulosaariHakutilannePath))
    await textEventuallyEquals(".card__header", "Hakeutumisvelvollisia (5)")

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
    await waitTableLoadingHasFinished(".hakutilanne")
  })

  it("Toimii passivoidun organisaation käyttäjällä", async () => {
    await loginAs(hakutilannePath, "valpas-aapajoen-koulu")

    await selectOrganisaatioByNimi("LAKKAUTETTU: Aapajoen koulu")
    await urlIsEventually(pathToUrl(aapajoenKouluHakutilannePath))
    await waitTableLoadingHasFinished(".hakutilanne")
    await textEventuallyEquals(".card__header", "Hakeutumisvelvollisia (5)")

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
    await waitTableLoadingHasFinished(".hakutilanne")
    await openOppijaView(kulosaarenOppijaOid)
    await urlIsEventually(
      pathToUrl(
        oppijaPath.href("/virkailija", {
          oppijaOid: kulosaarenOppijaOid,
          hakutilanneRef: kulosaarenAlaAsteOid,
        })
      )
    )

    await clickElement(".oppijaview__backbutton a")
    await urlIsEventually(pathToUrl(kulosaariHakutilannePath))
    await waitTableLoadingHasFinished(".hakutilanne")
  })

  it("Käyminen oppijakohtaisessa näkymässä ei hukkaa filttereiden tai järjestyksen tilaa", async () => {
    await loginAs(hakutilannePath, "valpas-jkl-normaali")

    // Vaihda filtteriä ja järjestyksen suuntaa nimen perusteella
    const selector = ".hakutilanne"
    const contentSelector = `${selector} tr td:first-child`
    await setTableTextFilter(selector, 1, "luoka")
    await toggleTableSort(selector, 1)

    // Ota snapshot talteen taulukon tilasta
    await waitTableLoadingHasFinished(".hakutilanne")
    const contentsBefore = await getTableContents(contentSelector)

    // Käy jossakin oppijanäkymässä
    await openAnyOppijaView()
    await expectElementEventuallyVisible(".oppijaview__backbutton a")
    await clickElement(".oppijaview__backbutton a")

    // Taulukon tilan pitäisi olla sama kuin aiemmin
    await urlIsEventually(pathToUrl(jklHakutilannePath))
    await waitTableLoadingHasFinished(".hakutilanne")
    const contentsAfter = await getTableContents(contentSelector)

    expect(contentsAfter).toEqual(contentsBefore)
  })

  it("Oppijasivulta, jolta puuttuu organisaatioreferenssi, ohjataan oikean organisaation hakutilannenäkymään", async () => {
    await loginAs(
      oppijaPath.href("/virkailija", {
        oppijaOid: kulosaarenOppijaOid,
      }),
      "valpas-useampi-peruskoulu"
    )

    await expectLinkToEqual(
      ".oppijaview__backbutton a",
      pathToUrl(kulosaariHakutilannePath)
    )

    await waitTableLoadingHasFinished(".hakutilanne")
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

    await waitTableLoadingHasFinished(".hakutilanne")
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
    await textEventuallyEquals(".card__header", "Hakeutumisvelvollisia (5)")

    await selectOrganisaatio(0)
    await textEventuallyEquals(".card__header", jklNormaalikouluTableHead)

    const stateAfterOrgChange = await getState()

    expect(stateBeforeOrgChange).toEqual(stateAfterOrgChange)

    await waitTableLoadingHasFinished(".hakutilanne")
  })

  it("Näyttää listan oppijoista international schoolille", async () => {
    await loginAs(hakutilannePath, "valpas-int-school")
    await urlIsEventually(pathToUrl(internationalSchoolHakutilannePath))
    await textEventuallyEquals(".card__header", internationalSchoolTableHead)
    await dataTableEventuallyEquals(
      ".hakutilanne",
      internationalSchoolTableContent,
      "|"
    )
  })

  it("Näyttää listan oppijoista European School of Helsingille", async () => {
    await loginAs(hakutilannePath, "valpas-esh")
    await urlIsEventually(pathToUrl(europeanSchoolOfHelsinkiHakutilannePath))
    await textEventuallyEquals(
      ".card__header",
      europeanSchoolOfHelsinkiTableHead
    )
    await dataTableEventuallyEquals(
      ".hakutilanne",
      europeanSchoolOfHelsinkiTableContent,
      "|"
    )
  })

  it("ESH: Ei näytä S5-opiskelijoita kevään valmistumisjakson päätyttyä", async () => {
    await loginAs(hakutilannePath, "valpas-esh", false, "2021-10-01")
    await urlIsEventually(pathToUrl(europeanSchoolOfHelsinkiHakutilannePath))
    await textEventuallyEquals(
      ".card__header",
      europeanSchoolOfHelsinkiTableHeadLater
    )
    await dataTableEventuallyEquals(
      ".hakutilanne",
      europeanSchoolOfHelsinkiTableContentLater,
      "|"
    )
  })

  it("ESH: Ei näytä S5-opiskelijoita seuraavan kevään valmistumisjakson aikana", async () => {
    await loginAs(hakutilannePath, "valpas-esh", false, "2022-09-05")
    await urlIsEventually(pathToUrl(europeanSchoolOfHelsinkiHakutilannePath))
    await textEventuallyEquals(
      ".card__header",
      europeanSchoolOfHelsinkiTableHeadLater
    )
    await dataTableEventuallyEquals(
      ".hakutilanne",
      europeanSchoolOfHelsinkiTableContentLater,
      "|"
    )
  })

  it("Epäonnistunut hakutietojen haku näyttää virheilmoituksen", async () => {
    await loginAs(hakutilannePath, "valpas-monta")
    await urlIsEventually(pathToUrl(saksalainenKouluHakutilannePath))
    await setTableTextFilter(".hakutilanne", 1, "sure-haut-aina-epäonnistuvat")
    await dataTableEventuallyEquals(
      ".hakutilanne",
      "Sure-haut-aina-epäonnistuvat Valpas  | 18.7.2004 | 9C  | – | Virhe oppijan hakuhistorian hakemisessa | – | – | – |",
      "|"
    )
  })
})

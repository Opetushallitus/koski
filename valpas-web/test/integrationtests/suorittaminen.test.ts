import { createSuorittaminenPathWithOrg } from "../../src/state/paths"
import {
  clickElement,
  expectElementEventuallyVisible,
  textEventuallyEquals,
} from "../integrationtests-env/browser/content"
import {
  goToLocation,
  pathToUrl,
  urlIsEventually,
} from "../integrationtests-env/browser/core"
import {
  dataTableEventuallyEquals,
  getTableContents,
  setTableTextFilter,
  toggleTableSort,
} from "../integrationtests-env/browser/datatable"
import { loginAs, resetMockData } from "../integrationtests-env/browser/reset"
import { hakutilannePath } from "../integrationtests/hakutilanne.shared"
import { jyväskylänNormaalikouluOid, stadinAmmattiopistoOid } from "./oids"
import {
  selectOrganisaatio,
  selectOrganisaatioByNimi,
} from "./organisaatiovalitsin-helpers"
import {
  jklNormaalikouluSuorittaminenTableContent,
  jklNormaalikouluSuorittaminenTableHead,
  stadinAmmattiopistoSuorittaminenTableContent,
  stadinAmmattiopistoSuorittaminenTableHead,
  suorittaminenListaHkiPath,
  suorittaminenListaJklPath,
  suorittaminenListaPath,
} from "./suorittaminen.shared"

const jklSuorittaminenPath = createSuorittaminenPathWithOrg(
  "/virkailija",
  jyväskylänNormaalikouluOid
)

const stadinAmmattiopistoSuorittaminenPath = createSuorittaminenPathWithOrg(
  "/virkailija",
  stadinAmmattiopistoOid
)

const viikinNormaalikouluId = "1.2.246.562.10.81927839589"
const viikinNormaalikouluSuorittaminenPath = createSuorittaminenPathWithOrg(
  "/virkailija",
  viikinNormaalikouluId
)

describe("Suorittamisen valvonta -näkymä", () => {
  it("Näyttää listan oppijoista Stadin ammattiopiston käyttäjälle", async () => {
    await loginAs(suorittaminenListaPath, "valpas-pelkkä-suorittaminen-amis")
    await urlIsEventually(pathToUrl(stadinAmmattiopistoSuorittaminenPath))

    await textEventuallyEquals(
      ".card__header",
      stadinAmmattiopistoSuorittaminenTableHead
    )
    await textEventuallyEquals(
      ".tabnavigation__item--selected",
      stadinAmmattiopistoSuorittaminenTableHead
    )

    await dataTableEventuallyEquals(
      ".suorittaminen",
      stadinAmmattiopistoSuorittaminenTableContent,
      "|"
    )
  })

  it("Näyttää listan oppijoista Jyväskylän normaalikoulun käyttäjälle", async () => {
    await loginAs(suorittaminenListaPath, "valpas-jkl-normaali")
    await urlIsEventually(pathToUrl(jklSuorittaminenPath))

    await resetMockData("2021-12-12")
    await goToLocation(jklSuorittaminenPath)

    await textEventuallyEquals(
      ".card__header",
      jklNormaalikouluSuorittaminenTableHead
    )
    await textEventuallyEquals(
      ".tabnavigation__item--selected",
      jklNormaalikouluSuorittaminenTableHead
    )
    await dataTableEventuallyEquals(
      ".suorittaminen",
      jklNormaalikouluSuorittaminenTableContent,
      "|"
    )
  })

  it("Näyttää tyhjän listan virheittä, jos ei oppijoita", async () => {
    await loginAs(suorittaminenListaPath, "valpas-viikin-normaalikoulu-2-aste")
    await urlIsEventually(pathToUrl(viikinNormaalikouluSuorittaminenPath))
    await textEventuallyEquals(".card__header", "Oppivelvolliset (0)")
  })

  it("Vaihtaa taulun sisällön organisaatiovalitsimesta", async () => {
    await loginAs(suorittaminenListaPath, "valpas-pelkkä-suorittaminen")

    await selectOrganisaatio(0)
    await urlIsEventually(pathToUrl(suorittaminenListaHkiPath))
    await textEventuallyEquals(".card__header", "Oppivelvolliset (0)")

    await selectOrganisaatio(1)
    await urlIsEventually(pathToUrl(suorittaminenListaJklPath))
    await textEventuallyEquals(".card__header", "Oppivelvolliset (11)")
  })

  it("Toimii koulutustoimijatason käyttäjällä", async () => {
    await loginAs(hakutilannePath, "valpas-hki-suorittaminen")
    await selectOrganisaatioByNimi("Stadin ammatti- ja aikuisopisto")

    await urlIsEventually(pathToUrl(stadinAmmattiopistoSuorittaminenPath))
    await textEventuallyEquals(
      ".card__header",
      stadinAmmattiopistoSuorittaminenTableHead
    )
    await dataTableEventuallyEquals(
      ".suorittaminen",
      stadinAmmattiopistoSuorittaminenTableContent,
      "|"
    )
  })

  it("Passiiviset organisaatiot listataan aktiivisten jälkeen", async () => {
    // TODO
  })

  it("Toimii passivoidun organisaation käyttäjällä", async () => {
    // TODO
  })

  it("Käyminen oppijakohtaisessa näkymässä ei hukkaa valittua organisaatiota, filttereiden tai järjestyksen tilaa", async () => {
    await loginAs(suorittaminenListaPath, "valpas-pelkkä-suorittaminen")

    await selectOrganisaatio(1)
    await urlIsEventually(pathToUrl(suorittaminenListaJklPath))

    // Vaihda filtteriä ja järjestyksen suuntaa nimen perusteella
    const selector = ".suorittaminen"
    await setTableTextFilter(selector, 1, "lukio")
    await toggleTableSort(selector, 1)

    // Ota snapshot talteen taulukon tilasta
    const contentsBefore = await getTableContents(selector)

    // Käy jossakin oppijanäkymässä
    await clickElement(
      `.suorittaminen .table__row:first-child td:first-child a`
    )
    await expectElementEventuallyVisible(".oppijaview__backbutton a")
    await clickElement(".oppijaview__backbutton a")

    // Taulukon tilan pitäisi olla sama kuin aiemmin
    await urlIsEventually(pathToUrl(suorittaminenListaJklPath))
    const contentsAfter = await getTableContents(selector)
    expect(contentsAfter).toEqual(contentsBefore)
  })
})

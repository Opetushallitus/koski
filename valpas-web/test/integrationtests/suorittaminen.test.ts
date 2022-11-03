import { Oid } from "../../src/state/common"
import { suorittaminenPathWithOrg } from "../../src/state/paths"
import {
  clickElement,
  expectElementEventuallyNotVisible,
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
  waitTableLoadingHasFinished,
} from "../integrationtests-env/browser/datatable"
import {
  loginAs,
  reset,
  resetMockData,
} from "../integrationtests-env/browser/reset"
import { hakutilannePath } from "../integrationtests/hakutilanne.shared"
import {
  Oppija,
  teeKuntailmoitusOppijanäkymistä,
  Tekijä,
} from "./kuntailmoitus.shared"
import {
  aapajoenKouluOid,
  internationalSchoolOid,
  jyväskylänNormaalikouluOid,
  stadinAmmattiopistoOid,
} from "./oids"
import {
  selectOrganisaatio,
  selectOrganisaatioByNimi,
  valitsimenOrganisaatiot,
} from "./organisaatiovalitsin-helpers"
import {
  internationalSchoolSuorittaminenTableContent,
  internationalSchoolSuorittaminenTableHead,
  jklNormaalikouluSuorittaminenTableContent,
  jklNormaalikouluSuorittaminenTableHead,
  stadinAmmattiopistoSuorittaminenTableContent,
  stadinAmmattiopistoSuorittaminenTableHead,
  suorittaminenKuntailmoitusListaJklPath,
  suorittaminenListaHkiPath,
  suorittaminenListaJklPath,
  suorittaminenListaPath,
} from "./suorittaminen.shared"

const jklSuorittaminenPath = suorittaminenPathWithOrg.href(
  "/virkailija",
  jyväskylänNormaalikouluOid
)

const aapajokiSuorittaminenPath = suorittaminenPathWithOrg.href(
  "/virkailija",
  aapajoenKouluOid
)

const stadinAmmattiopistoSuorittaminenPath = suorittaminenPathWithOrg.href(
  "/virkailija",
  stadinAmmattiopistoOid
)

const internationalSchoolSuorittaminenPath = suorittaminenPathWithOrg.href(
  "/virkailija",
  internationalSchoolOid
)

const viikinNormaalikouluId = "1.2.246.562.10.81927839589"
const viikinNormaalikouluSuorittaminenPath = suorittaminenPathWithOrg.href(
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
    await dataTableEventuallyEquals(
      ".suorittaminen",
      jklNormaalikouluSuorittaminenTableContent,
      "|"
    )
  })

  it("Näyttää listan oppijoista International schoolin käyttäjälle", async () => {
    await loginAs(suorittaminenListaPath, "valpas-int-school")
    await urlIsEventually(pathToUrl(internationalSchoolSuorittaminenPath))

    await goToLocation(internationalSchoolSuorittaminenPath)

    await textEventuallyEquals(
      ".card__header",
      internationalSchoolSuorittaminenTableHead
    )
    await dataTableEventuallyEquals(
      ".suorittaminen",
      internationalSchoolSuorittaminenTableContent,
      "|"
    )
  })

  it("Näyttää tyhjän listan virheittä, jos ei oppijoita", async () => {
    await loginAs(suorittaminenListaPath, "valpas-viikin-normaalikoulu-2-aste")
    await urlIsEventually(pathToUrl(viikinNormaalikouluSuorittaminenPath))
    await textEventuallyEquals(".card__header", "Oppivelvolliset (0)")
    await waitTableLoadingHasFinished(".suorittaminen")
  })

  it("Vaihtaa taulun sisällön organisaatiovalitsimesta", async () => {
    await loginAs(suorittaminenListaPath, "valpas-pelkkä-suorittaminen")

    await selectOrganisaatio(0)
    await urlIsEventually(pathToUrl(suorittaminenListaHkiPath))
    await textEventuallyEquals(".card__header", "Oppivelvolliset (0)")

    await selectOrganisaatio(1)
    await urlIsEventually(pathToUrl(suorittaminenListaJklPath))
    await textEventuallyEquals(".card__header", "Oppivelvolliset (21)")
    await waitTableLoadingHasFinished(".suorittaminen")
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
    await loginAs(hakutilannePath, "valpas-pelkkä-suorittaminen")

    const organisaatiot = await valitsimenOrganisaatiot()

    const expectedOrganisaatiot = [
      "Helsingin medialukio (1.2.246.562.10.70411521654)",
      "Jyväskylän normaalikoulu (1.2.246.562.10.14613773812)",
      "LAKKAUTETTU: Aapajoen koulu (1.2.246.562.10.26197302388)",
    ]

    expect(organisaatiot).toEqual(expectedOrganisaatiot)
    await waitTableLoadingHasFinished(".suorittaminen")
  })

  it("Toimii passivoidun organisaation käyttäjällä", async () => {
    await loginAs(hakutilannePath, "valpas-aapajoen-koulu")
    await goToLocation(aapajokiSuorittaminenPath)
    await selectOrganisaatioByNimi("LAKKAUTETTU: Aapajoen koulu")
    await urlIsEventually(pathToUrl(aapajokiSuorittaminenPath))
    await textEventuallyEquals(".card__header", "Oppivelvolliset (0)")
    await waitTableLoadingHasFinished(".suorittaminen")
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
    await waitTableLoadingHasFinished(".suorittaminen")
    const contentsBefore = await getTableContents(selector)

    // Käy jossakin oppijanäkymässä
    await clickElement(
      `.suorittaminen .table__row:first-child td:first-child a`
    )
    await expectElementEventuallyVisible(".oppijaview__backbutton a")
    await clickElement(".oppijaview__backbutton a")

    // Taulukon tilan pitäisi olla sama kuin aiemmin
    await urlIsEventually(pathToUrl(suorittaminenListaJklPath))
    await waitTableLoadingHasFinished(".suorittaminen")
    const contentsAfter = await getTableContents(selector)
    expect(contentsAfter).toEqual(contentsBefore)
  })

  it("Kuntailmoituksen tekeminen oppijasta, jolla on voimassaoleva opiskeluoikeus, ei poista riviä listasta, mutta lisää rivin ilmoituslistaan", async () => {
    const oppijaOid = "1.2.246.562.24.00000000030"
    const oppijaRowSelector = `.table__row[data-row*="${oppijaOid}"]`

    await reset(suorittaminenListaPath, true)
    await loginAs(suorittaminenListaPath, "valpas-jkl-normaali")
    await expectElementEventuallyVisible(oppijaRowSelector)

    await teeKuntailmoitus(
      oppijaOid,
      "Jkl-Lukio-Kulosaarelainen Valpas",
      "010104A187H"
    )

    await goToLocation(suorittaminenListaJklPath)
    await expectElementEventuallyVisible(oppijaRowSelector)

    await goToLocation(suorittaminenKuntailmoitusListaJklPath)
    await expectElementEventuallyVisible(oppijaRowSelector)
    await waitTableLoadingHasFinished(".suorittaminen")
  })

  it("Kuntailmoituksen tekeminen oppijasta, jolla ei ole voimassaolevaa opiskeluoikeuttaa, poistaa rivin listasta ja lisää uuden rivin ilmoituslistaan", async () => {
    const oppijaOid = "1.2.246.562.24.00000000052"
    const oppijaRowSelector = `.table__row[data-row*="${oppijaOid}"]`

    await reset(suorittaminenListaPath, true)
    await loginAs(suorittaminenListaPath, "valpas-jkl-normaali")
    await expectElementEventuallyVisible(oppijaRowSelector)

    await teeKuntailmoitus(
      oppijaOid,
      "Lukio-opiskelija-valmistunut Valpas",
      "271105A835H"
    )

    await goToLocation(suorittaminenListaJklPath)
    await expectElementEventuallyNotVisible(oppijaRowSelector)

    await goToLocation(suorittaminenKuntailmoitusListaJklPath)
    await expectElementEventuallyVisible(oppijaRowSelector)
    await waitTableLoadingHasFinished(".suorittaminen")
  })
})

const teeKuntailmoitus = async (oppijaOid: Oid, nimi: string, hetu: string) => {
  const tekijä: Tekijä = {
    nimi: "käyttäjä valpas-jkl-normaali",
    email: "suorittaminen@oph.fi",
    puhelin: "0505050505050",
    organisaatio: "Jyväskylän normaalikoulu",
    organisaatioOid: "1.2.246.562.10.14613773812",
  }

  const oppija: Oppija = {
    oid: oppijaOid,
    title: `${nimi} (${hetu})`,
    prefill: 0,
    expected: {
      kohde: "Helsinki",
      tekijä: [
        tekijä.nimi,
        tekijä.email,
        tekijä.puhelin,
        tekijä.organisaatio,
      ].join("\n"),
      email: "valpas@gmail.com",
      lähiosoite: "Esimerkkitie 10",
      maa: "Costa Rica",
      muuHaku: "Ei",
      postitoimipaikka: "99999 Helsinki",
      puhelin: "0401122334",
    },
  }

  await teeKuntailmoitusOppijanäkymistä([oppija], tekijä)
}

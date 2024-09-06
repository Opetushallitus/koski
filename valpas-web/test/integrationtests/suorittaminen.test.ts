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
  europeanSchoolOfHelsinkiOid,
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
  europeanSchoolOfHelsinki2023SuorittaminenTableContent,
  europeanSchoolOfHelsinki2023SuorittaminenTableHead,
  europeanSchoolOfHelsinkiSuorittaminenTableContent,
  europeanSchoolOfHelsinkiSuorittaminenTableHead,
  internationalSchoolSuorittaminenTableContent,
  internationalSchoolSuorittaminenTableHead,
  jklNormaalikouluSuorittaminenTableContent,
  jklNormaalikouluSuorittaminenTableHead,
  stadinAmmattiopistoSuorittaminen20230531TableContent,
  stadinAmmattiopistoSuorittaminenTableContent,
  stadinAmmattiopistoSuorittaminenTableHead,
  suorittaminenKuntailmoitusListaJklPath,
  suorittaminenListaHkiPath,
  suorittaminenListaJklPath,
  suorittaminenListaPath,
} from "./suorittaminen.shared"

const jklSuorittaminenPath = suorittaminenPathWithOrg.href(
  "/virkailija",
  jyväskylänNormaalikouluOid,
)

const aapajokiSuorittaminenPath = suorittaminenPathWithOrg.href(
  "/virkailija",
  aapajoenKouluOid,
)

const stadinAmmattiopistoSuorittaminenPath = suorittaminenPathWithOrg.href(
  "/virkailija",
  stadinAmmattiopistoOid,
)

const internationalSchoolSuorittaminenPath = suorittaminenPathWithOrg.href(
  "/virkailija",
  internationalSchoolOid,
)

const europeanSchoolOfHelsinkiSuorittaminenPath = suorittaminenPathWithOrg.href(
  "/virkailija",
  europeanSchoolOfHelsinkiOid,
)

const viikinNormaalikouluId = "1.2.246.562.10.81927839589"
const viikinNormaalikouluSuorittaminenPath = suorittaminenPathWithOrg.href(
  "/virkailija",
  viikinNormaalikouluId,
)

describe("Suorittamisen valvonta -näkymä", () => {
  it("Näyttää listan oppijoista Stadin ammattiopiston käyttäjälle", async () => {
    await loginAs(
      suorittaminenListaPath,
      "valpas-pelkkä-suorittaminen-amis",
      true,
      "2021-09-02",
    )
    await urlIsEventually(pathToUrl(stadinAmmattiopistoSuorittaminenPath))

    await textEventuallyEquals(
      ".card__header",
      stadinAmmattiopistoSuorittaminenTableHead,
    )
    await dataTableEventuallyEquals(
      ".suorittaminen",
      stadinAmmattiopistoSuorittaminenTableContent,
      "|",
    )
  })

  it("Näyttää listan oppijoista International schoolin käyttäjälle", async () => {
    await loginAs(suorittaminenListaPath, "valpas-int-school")
    await urlIsEventually(pathToUrl(internationalSchoolSuorittaminenPath))

    await goToLocation(internationalSchoolSuorittaminenPath)

    await textEventuallyEquals(
      ".card__header",
      internationalSchoolSuorittaminenTableHead,
    )
    await dataTableEventuallyEquals(
      ".suorittaminen",
      internationalSchoolSuorittaminenTableContent,
      "|",
    )
  })

  it("Näyttää listan oppijoista European School of Helsingin käyttäjälle", async () => {
    await loginAs(suorittaminenListaPath, "valpas-esh")
    await urlIsEventually(pathToUrl(europeanSchoolOfHelsinkiSuorittaminenPath))

    await goToLocation(europeanSchoolOfHelsinkiSuorittaminenPath)

    await textEventuallyEquals(
      ".card__header",
      europeanSchoolOfHelsinkiSuorittaminenTableHead,
    )
    await dataTableEventuallyEquals(
      ".suorittaminen",
      europeanSchoolOfHelsinkiSuorittaminenTableContent,
      "|",
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
    await loginAs(
      hakutilannePath,
      "valpas-hki-suorittaminen",
      true,
      "2021-09-02",
    )
    await selectOrganisaatioByNimi("Stadin ammatti- ja aikuisopisto")

    await urlIsEventually(pathToUrl(stadinAmmattiopistoSuorittaminenPath))
    await textEventuallyEquals(
      ".card__header",
      stadinAmmattiopistoSuorittaminenTableHead,
    )
    await dataTableEventuallyEquals(
      ".suorittaminen",
      stadinAmmattiopistoSuorittaminenTableContent,
      "|",
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
      `.suorittaminen .table__row:first-child td:first-child a`,
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
      "010104A187H",
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
      "271105A835H",
    )

    await goToLocation(suorittaminenListaJklPath)
    await expectElementEventuallyNotVisible(oppijaRowSelector)

    await goToLocation(suorittaminenKuntailmoitusListaJklPath)
    await expectElementEventuallyVisible(oppijaRowSelector)
    await waitTableLoadingHasFinished(".suorittaminen")
  })

  it("Opiskeluoikeudesta eronnut ja samana päivänä uuden opiskeluoikeuden aloittanut näytetään vain uuden oppilaitoksen suorittamisen valvonnassa", async () => {
    // Amis-eronnut-uusi-oo-samana-päivänä Valpas (300305C243W)
    const oppijaOid = "1.2.246.562.24.00000000067"
    const oppijaRowSelector = `.table__row[data-row*="${oppijaOid}"]`

    await reset(suorittaminenListaPath, true, "2021-09-02")
    await loginAs(
      suorittaminenListaPath,
      "valpas-pelkkä-suorittaminen-amis",
      false,
      "2021-09-02",
    )
    await expectElementEventuallyNotVisible(oppijaRowSelector)

    await loginAs(
      suorittaminenListaPath,
      "valpas-pelkkä-suorittaminen-amis-omnia",
      false,
      "2021-09-02",
    )
    await expectElementEventuallyVisible(oppijaRowSelector)
  })

  it("Kahdesta opiskeluoikeudesta eronnut oppija näkyy vain viimeisimmän oppilaitoksen suorittamisen valvonnassa eron jälkeen", async () => {
    // Amis-eronnut-uusi-oo-samana-päivänä-jo-päättynyt Valpas (140305D021D)
    const oppijaOid = "1.2.246.562.24.00000000068"
    const oppijaRowSelector = `.table__row[data-row*="${oppijaOid}"]`

    await reset(suorittaminenListaPath, true, "2022-03-02")
    await loginAs(
      suorittaminenListaPath,
      "valpas-pelkkä-suorittaminen-amis",
      false,
      "2022-03-02",
    )
    await expectElementEventuallyNotVisible(oppijaRowSelector)

    await loginAs(
      suorittaminenListaPath,
      "valpas-pelkkä-suorittaminen-amis-omnia",
      false,
      "2022-03-02",
    )
    await expectElementEventuallyVisible(oppijaRowSelector)
  })

  it("Kahdesta opiskeluoikeudesta eronnut oppija näkyy vain viimeisimmän oppilaitoksen suorittamisen valvonnassa eron jälkeen, kun toinen opiskeluoikeus oli nivelvaihetta", async () => {
    // Amis-eronnut-uusi-nivelvaihe-oo-samana-päivänä-jo-päättynyt Valpas (240305A783E)
    const oppijaOid = "1.2.246.562.24.00000000069"
    const oppijaRowSelector = `.table__row[data-row*="${oppijaOid}"]`

    await reset(suorittaminenListaPath, true, "2022-03-02")
    await loginAs(
      suorittaminenListaPath,
      "valpas-pelkkä-suorittaminen-amis",
      false,
      "2022-03-02",
    )
    await expectElementEventuallyNotVisible(oppijaRowSelector)

    await loginAs(
      suorittaminenListaPath,
      "valpas-pelkkä-suorittaminen-ressu",
      false,
      "2022-03-02",
    )
    await expectElementEventuallyVisible(oppijaRowSelector)
  })

  it("Kahdesta opiskeluoikeudesta eronnut oppija näkyy vain viimeisimmän oppilaitoksen suorittamisen valvonnassa eron jälkeen, kun toinen opiskeluoikeus ajoittuu ensimmäisen sisälle", async () => {
    // Amis-eronnut-uusi-oo-alkanut-ja-päättynyt-eroon-keskellä Valpas (170205A609H)
    const oppijaOid = "1.2.246.562.24.00000000070"
    const oppijaRowSelector = `.table__row[data-row*="${oppijaOid}"]`

    await reset(suorittaminenListaPath, true, "2022-03-02")
    await loginAs(
      suorittaminenListaPath,
      "valpas-pelkkä-suorittaminen-amis",
      false,
      "2022-03-02",
    )
    await expectElementEventuallyVisible(oppijaRowSelector)

    await loginAs(
      suorittaminenListaPath,
      "valpas-pelkkä-suorittaminen-amis-omnia",
      false,
      "2022-03-02",
    )
    await expectElementEventuallyNotVisible(oppijaRowSelector)
  })

  it("Suorittamisen valvonta ei näytä oppijaa, joka on valmistunut amiksesta, mutta on alle 18v", async () => {
    // Amis-valmistunut-eronnut-valmasta Valpas (180605A313U) ei näy listalla
    await loginAs(
      suorittaminenListaPath,
      "valpas-pelkkä-suorittaminen-amis",
      true,
      "2023-05-31",
    )
    await urlIsEventually(pathToUrl(stadinAmmattiopistoSuorittaminenPath))

    await textEventuallyEquals(".card__header", "Oppivelvolliset (6)")
    await dataTableEventuallyEquals(
      ".suorittaminen",
      stadinAmmattiopistoSuorittaminen20230531TableContent,
      "|",
    )
  })

  it("Suorittamisen valvonta ei näytä oppijaa, joka on yli 18v (ja valmistunut amiksesta)", async () => {
    // Amis-valmistunut-eronnut-valmasta Valpas (180605A313U) ei näy listalla
    await loginAs(
      suorittaminenListaPath,
      "valpas-pelkkä-suorittaminen-amis",
      true,
      "2023-06-18",
    )
    await urlIsEventually(pathToUrl(stadinAmmattiopistoSuorittaminenPath))

    await textEventuallyEquals(".card__header", "Oppivelvolliset (6)")
    await dataTableEventuallyEquals(
      ".suorittaminen",
      stadinAmmattiopistoSuorittaminen20230531TableContent,
      "|",
    )
  })

  describe("Kun tarkastelupäivää vaihdetaan", () => {
    it("Näyttää listan oppijoista Jyväskylän normaalikoulun käyttäjälle", async () => {
      await loginAs(suorittaminenListaPath, "valpas-jkl-normaali")
      await urlIsEventually(pathToUrl(jklSuorittaminenPath))

      await resetMockData("2021-12-12")

      await goToLocation(jklSuorittaminenPath)

      await textEventuallyEquals(
        ".card__header",
        jklNormaalikouluSuorittaminenTableHead,
      )
      await dataTableEventuallyEquals(
        ".suorittaminen",
        jklNormaalikouluSuorittaminenTableContent,
        "|",
      )
    })
  })

  it("Näyttää ESH:ssa vain ESH:ta suorittavat, ei pelkkää EB:tä suorittavia", async () => {
    await loginAs(suorittaminenListaPath, "valpas-esh")
    await urlIsEventually(pathToUrl(europeanSchoolOfHelsinkiSuorittaminenPath))

    await resetMockData("2023-06-20")

    await goToLocation(europeanSchoolOfHelsinkiSuorittaminenPath)

    await textEventuallyEquals(
      ".card__header",
      europeanSchoolOfHelsinki2023SuorittaminenTableHead,
    )
    await dataTableEventuallyEquals(
      ".suorittaminen",
      europeanSchoolOfHelsinki2023SuorittaminenTableContent,
      "|",
    )
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

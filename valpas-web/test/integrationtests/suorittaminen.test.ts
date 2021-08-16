import { createSuorittaminenPathWithOrg } from "../../src/state/paths"
import { textEventuallyEquals } from "../integrationtests-env/browser/content"
import {
  goToLocation,
  pathToUrl,
  urlIsEventually,
} from "../integrationtests-env/browser/core"
import { dataTableEventuallyEquals } from "../integrationtests-env/browser/datatable"
import { loginAs, resetMockData } from "../integrationtests-env/browser/reset"
import { jyväskylänNormaalikouluOid, stadinAmmattiopistoOid } from "./oids"
import {
  jklNormaalikouluSuorittaminenTableContent,
  jklNormaalikouluSuorittaminenTableHead,
  stadinAmmattiopistoSuorittaminenTableContent,
  stadinAmmattiopistoSuorittaminenTableHead,
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

  it("Näyttää tyhjän listan virheittä, jos ei oppijoita", async () => {
    await loginAs(suorittaminenListaPath, "valpas-viikin-normaalikoulu-2-aste")
    await urlIsEventually(pathToUrl(viikinNormaalikouluSuorittaminenPath))
    await textEventuallyEquals(".card__header", "Oppivelvolliset (0)")
  })

  it("Vaihtaa taulun sisällön organisaatiovalitsimesta", async () => {
    // TODO
  })

  it("Toimii koulutustoimijatason käyttäjällä", async () => {
    // TODO
  })

  it("Passiiviset organisaatiot listataan aktiivisten jälkeen", async () => {
    // TODO
  })

  it("Toimii passivoidun organisaation käyttäjällä", async () => {
    // TODO
  })

  it("Käyminen oppijakohtaisessa näkymässä ei hukkaa valittua organisaatiota", async () => {
    // TODO
  })

  it("Käyminen oppijakohtaisessa näkymässä ei hukkaa filttereiden tai järjestyksen tilaa", async () => {
    // TODO
  })
})

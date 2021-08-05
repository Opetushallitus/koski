import { createSuorittaminenPathWithOrg } from "../../src/state/paths"
import { textEventuallyEquals } from "../integrationtests-env/browser/content"
import {
  pathToUrl,
  urlIsEventually,
} from "../integrationtests-env/browser/core"
import { dataTableEventuallyEquals } from "../integrationtests-env/browser/datatable"
import { loginAs } from "../integrationtests-env/browser/reset"
import { jyväskylänNormaalikouluOid } from "./oids"
import {
  jklNormaalikouluSuorittaminenTableContent,
  jklNormaalikouluSuorittaminenTableHead,
  suorittaminenListaPath,
} from "./suorittaminen.shared"

const jklSuorittaminenPath = createSuorittaminenPathWithOrg(
  "/virkailija",
  jyväskylänNormaalikouluOid
)

const saksalainenKouluOid = "1.2.246.562.10.45093614456"
const saksalainenKouluSuorittaminenPath = createSuorittaminenPathWithOrg(
  "/virkailija",
  saksalainenKouluOid
)

describe("Suorittamisen valvonta -näkymä", () => {
  it("Näyttää listan oppijoista", async () => {
    await loginAs(suorittaminenListaPath, "valpas-jkl-normaali")
    await urlIsEventually(pathToUrl(jklSuorittaminenPath))
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
    await loginAs(suorittaminenListaPath, "valpas-saksalainen-2-aste")
    await urlIsEventually(pathToUrl(saksalainenKouluSuorittaminenPath))
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

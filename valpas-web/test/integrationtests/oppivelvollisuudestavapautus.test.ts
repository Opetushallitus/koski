import { oppijaPath } from "../../src/state/paths"
import {
  allowNetworkError,
  FORBIDDEN,
} from "../integrationtests-env/browser/fail-on-console"
import { loginAs } from "../integrationtests-env/browser/reset"
import {
  mainHeadingEquals,
  oppivelvollisuustiedot,
  oppivelvollisuustiedotEquals,
  secondaryHeadingEquals,
} from "./oppija.shared"

const oppivelvollisuudestaVapautettuPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000157",
})

const tarkastelupäivä = "2021-10-01"

describe("Oppivelvollisuudesta vapauttaminen", () => {
  describe("Oppivelvollisuudesta vapautetusta oppijasta ei näytetä kuin minimitiedot", () => {
    it("Vapautuksen tehnyt kuntakäyttäjä näkee ja voi editoida", async () => {
      await loginAs(
        oppivelvollisuudestaVapautettuPath,
        "valpas-monta",
        false,
        tarkastelupäivä
      )
      await validateOppivelvollisuudestaVapautettu(true)
    })

    it("Joku muu kuntakäyttäjä näkee", async () => {
      await loginAs(
        oppivelvollisuudestaVapautettuPath,
        "valpas-tornio",
        false,
        tarkastelupäivä
      )
      await validateOppivelvollisuudestaVapautettu(false)
    })

    it("Maksuttomuuskäyttäjä näkee", async () => {
      await loginAs(
        oppivelvollisuudestaVapautettuPath,
        "valpas-pelkkä-maksuttomuus",
        false,
        tarkastelupäivä
      )
      await validateOppivelvollisuudestaVapautettu(false)
    })

    it("Hakeutumisen valvoja ei näe mitään", async () => {
      await loginAs(
        oppivelvollisuudestaVapautettuPath,
        "valpas-jkl-normaali-perus",
        false,
        tarkastelupäivä
      )
      await validateOppivelvollisuudestaVapautettuEiNäy()
    })

    it("Suorittamisen valvoja ei näe mitään", async () => {
      await loginAs(
        oppivelvollisuudestaVapautettuPath,
        "valpas-jkl-yliopisto-suorittaminen",
        false,
        tarkastelupäivä
      )
      await validateOppivelvollisuudestaVapautettuEiNäy()
    })
  })
})

const validateOppivelvollisuudestaVapautettu = async (
  vapautuksenMitätöintiSallittu: boolean
) => {
  await mainHeadingEquals(
    "Oppivelvollisuudesta-vapautettu Valpas (060605A538B)"
  )
  await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000157")
  await oppivelvollisuustiedotEquals(
    oppivelvollisuustiedot({
      oppivelvollisuus:
        "Vapautettu oppivelvollisuudesta 1.8.2021 alkaen, myöntäjä Helsingin kaupunki",
      vapautuksenMitätöintiBtn: vapautuksenMitätöintiSallittu,
      maksuttomuusoikeus: "31.7.2021 asti",
    })
  )
  // TODO: Lisää tarkastukset, ettei hakuja yms. näy
}

const validateOppivelvollisuudestaVapautettuEiNäy = async () => {
  allowNetworkError("/api/oppija/1.2.246.562.24.00000000157", FORBIDDEN)
  await mainHeadingEquals("Oppijan tiedot")
  await secondaryHeadingEquals(
    "Oppijaa ei löydy tunnuksella 1.2.246.562.24.00000000157"
  )
}

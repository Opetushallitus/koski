import { oppijaPath } from "../../src/state/paths"
import {
  clickElement,
  expectElementEventuallyVisible,
  testId,
} from "../integrationtests-env/browser/content"
import {
  allowNetworkError,
  FORBIDDEN,
} from "../integrationtests-env/browser/fail-on-console"
import {
  loginAs,
  resetRaportointikanta,
} from "../integrationtests-env/browser/reset"
import {
  mainHeadingEquals,
  oppivelvollisuustiedot,
  oppivelvollisuustiedotEquals,
  secondaryHeadingEquals,
} from "./oppija.shared"

const vapautetettavaPath = oppijaPath.href("/virkailija", {
  // Oppivelvollinen-ysiluokka-kesken-keväällä-2021 Valpas (221105A3023)
  oppijaOid: "1.2.246.562.24.00000000001",
})

const oppivelvollisuudestaVapautettuPath = oppijaPath.href("/virkailija", {
  // Oppivelvollisuudesta-vapautettu Valpas (060605A538B)
  oppijaOid: "1.2.246.562.24.00000000161",
})

const tarkastelupäivä = "2021-10-01"

describe("Oppivelvollisuudesta vapauttaminen", () => {
  describe("Oppivelvollisuudesta vapautetusta oppijasta ei näytetä kuin minimitiedot", () => {
    it("Vapautuksen tehnyt kuntakäyttäjä näkee ja voi editoida", async () => {
      await loginAs(
        oppivelvollisuudestaVapautettuPath,
        "valpas-monta",
        false,
        tarkastelupäivä,
      )
      await validateOppivelvollisuudestaVapautettu(true)
    })

    it("Joku muu kuntakäyttäjä näkee", async () => {
      await loginAs(
        oppivelvollisuudestaVapautettuPath,
        "valpas-tornio",
        false,
        tarkastelupäivä,
      )
      await validateOppivelvollisuudestaVapautettu(false)
    })

    it("Maksuttomuuskäyttäjä näkee", async () => {
      await loginAs(
        oppivelvollisuudestaVapautettuPath,
        "valpas-pelkkä-maksuttomuus",
        false,
        tarkastelupäivä,
      )
      await validateOppivelvollisuudestaVapautettu(false)
    })

    it("Hakeutumisen valvoja ei näe mitään", async () => {
      await loginAs(
        oppivelvollisuudestaVapautettuPath,
        "valpas-jkl-normaali-perus",
        false,
        tarkastelupäivä,
      )
      await validateOppivelvollisuudestaVapautettuEiNäy()
    })

    it("Suorittamisen valvoja ei näe mitään", async () => {
      await loginAs(
        oppivelvollisuudestaVapautettuPath,
        "valpas-jkl-yliopisto-suorittaminen",
        false,
        tarkastelupäivä,
      )
      await validateOppivelvollisuudestaVapautettuEiNäy()
    })
  })

  describe("Vapautuksen merkitseminen", () => {
    it("Happy path: vapautus", async () => {
      await loginAs(vapautetettavaPath, "valpas-monta", true, tarkastelupäivä)

      await merkitseOvVapautus()

      await oppivelvollisuustiedotEquals(
        oppivelvollisuustiedot({
          oppivelvollisuus:
            "Vapautettu oppivelvollisuudesta 1.10.2021 alkaen, myöntäjä Helsingin kaupunki",
          vapautuksenMitätöintiBtn: true,
          maksuttomuusoikeus: "30.9.2021 asti",
        }),
      )
    })
  })

  it("Happy path: mitätöinti", async () => {
    const validateTiedot = () =>
      oppivelvollisuustiedotEquals(
        oppivelvollisuustiedot({
          opiskelutilanne: "Kyllä",
          oppivelvollisuus: "21.11.2023 asti",
          maksuttomuusoikeus: "31.12.2025 asti",
          oppivelvollisuudenKeskeytysBtn: true,
          kuntailmoitusBtn: true,
          merkitseVapautusBtn: true,
        }),
      )

    await loginAs(vapautetettavaPath, "valpas-monta", true, tarkastelupäivä)

    await validateTiedot()

    await merkitseOvVapautus()
    await mitätöiOvVapautus()

    await validateTiedot()
  })

  it("Ennen raportointikannan viimeisintä päivitystä tehdyn vapautuksen mitätöinti", async () => {
    await loginAs(vapautetettavaPath, "valpas-monta", true, tarkastelupäivä)

    await merkitseOvVapautus()
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        oppivelvollisuus:
          "Vapautettu oppivelvollisuudesta 1.10.2021 alkaen, myöntäjä Helsingin kaupunki",
        vapautuksenMitätöintiBtn: true,
        maksuttomuusoikeus: "30.9.2021 asti",
      }),
    )

    await resetRaportointikanta()

    await mitätöiOvVapautus()
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        oppivelvollisuus:
          "Vapautus oppivelvollisuudesta on mitätöitymässä. Oppivelvollisuuden päättymispäivää ja oikeuden maksuttomaan koulutuksen päättymispäivää ei pystytä näyttämään tällä hetkellä. Tieto on saatavilla tavallisesti noin vuorokauden kuluessa.",
        oppivelvollisuudenKeskeytysBtn: true,
        kuntailmoitusBtn: true,
        merkitseVapautusBtn: true,
      }),
    )
  })
})

const merkitseOvVapautus = async () => {
  await expectElementEventuallyVisible(testId("ovvapautus-btn"))
  await clickElement(testId("ovvapautus-btn"))

  await expectElementEventuallyVisible(testId("ovvapautus-vahvistus"))
  await clickElement(testId("ovvapautus-vahvistus"))
  await clickElement(testId("ovvapautus-submit"))

  await expectElementEventuallyVisible(testId("confirm-yes"))
  await clickElement(testId("confirm-yes"))
}

const mitätöiOvVapautus = async () => {
  await expectElementEventuallyVisible(testId("ovvapautus-mitatointi-btn"))
  await clickElement(testId("ovvapautus-mitatointi-btn"))

  await clickElement(testId("ovvapautus-submit"))

  await expectElementEventuallyVisible(testId("confirm-yes"))
  await clickElement(testId("confirm-yes"))
}

const validateOppivelvollisuudestaVapautettu = async (
  vapautuksenMitätöintiSallittu: boolean,
) => {
  await mainHeadingEquals(
    "Oppivelvollisuudesta-vapautettu Valpas (060605A538B)",
  )
  await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000161")
  await oppivelvollisuustiedotEquals(
    oppivelvollisuustiedot({
      oppivelvollisuus:
        "Vapautettu oppivelvollisuudesta 1.8.2000 alkaen, myöntäjä Helsingin kaupunki",
      vapautuksenMitätöintiBtn: vapautuksenMitätöintiSallittu,
      maksuttomuusoikeus: "31.7.2000 asti",
    }),
  )
}

const validateOppivelvollisuudestaVapautettuEiNäy = async () => {
  allowNetworkError("/api/oppija/1.2.246.562.24.00000000161", FORBIDDEN)
  await mainHeadingEquals("Oppijan tiedot")
  await secondaryHeadingEquals(
    "Oppijaa ei löydy tunnuksella 1.2.246.562.24.00000000161",
  )
}

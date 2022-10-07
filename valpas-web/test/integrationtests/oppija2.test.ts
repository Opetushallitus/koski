import { oppijaPath } from "../../src/state/paths"
import {
  clickElement,
  contentEventuallyEquals,
  testId,
  textEventuallyEquals,
} from "../integrationtests-env/browser/content"
import {
  acceptConfirmation,
  goToLocation,
} from "../integrationtests-env/browser/core"
import {
  allowNetworkError,
  BAD_REQUEST,
  FORBIDDEN,
} from "../integrationtests-env/browser/fail-on-console"
import { loginAs, resetMockData } from "../integrationtests-env/browser/reset"
import {
  expectEiKuntailmoituksiaNotVisible,
  historiaOpintoOikeus,
  historiaOppivelvollisuudenKeskeytys,
  historiaOppivelvollisuudenKeskeytysToistaiseksi,
  historiaVastuuilmoitus,
  merge,
  opiskeluhistoriaEquals,
  oppivelvollisuustiedot,
  oppivelvollisuustiedotEquals,
} from "./oppija.shared"

const lukioOpiskelijaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000004",
})

const oppivelvollisuusKeskeytettyMääräajaksiPath = oppijaPath.href(
  "/virkailija",
  {
    oppijaOid: "1.2.246.562.24.00000000056",
  }
)
const oppivelvollisuusKeskeytettyToistaiseksiPath = oppijaPath.href(
  "/virkailija",
  {
    oppijaOid: "1.2.246.562.24.00000000057",
  }
)
const eiOppivelvollisuudenSuorittamiseenKelpaaviaOpiskeluoikeuksiaPath = oppijaPath.href(
  "/virkailija",
  {
    oppijaOid: "1.2.246.562.24.00000000058",
  }
)
const hetutonPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000059",
})

const opiskeluoikeusKeskeytettyMääräajaksiPath = oppijaPath.href(
  "/virkailija",
  {
    oppijaOid: "1.2.246.562.24.00000000077",
  }
)

const opiskeluoikeusLomaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000078",
})

const opiskeluoikeusValmaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000062",
})

const maksuttomuuttaPidennettyPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000127",
})

const perusopetukseenValmistautuvaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000131",
})

const montaKuntailmoitustaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000041",
})

const preIBdOppijaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000135",
})

const oppivelvollisuusKeskeytettyHelsinkiläinenPath = oppijaPath.href(
  "/virkailija",
  {
    oppijaOid: "1.2.246.562.24.00000000130",
  }
)

const eiKoskessaOppivelvollinenJollaKeskeytyksiäJaIlmoituksiaPath = oppijaPath.href(
  "/virkailija",
  {
    oppijaOid: "1.2.246.562.24.00000000144",
  }
)

const pelkkäEsiopetusPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000126",
})

const mainHeadingEquals = (expected: string) =>
  textEventuallyEquals("h1.heading--primary", expected)
const secondaryHeadingEquals = (expected: string) =>
  textEventuallyEquals(".oppijaview__secondaryheading", expected)

describe("Oppijakohtainen näkymä 2/2", () => {
  it("Näyttää detaljisivun suorittamisen valvojalle lukio-oppijasta", async () => {
    await loginAs(lukioOpiskelijaPath, "valpas-pelkkä-suorittaminen")

    await mainHeadingEquals("Lukio-opiskelija Valpas (070504A717P)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000004")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "6.5.2022 asti",
        maksuttomuusoikeus: "31.12.2024 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      historiaOpintoOikeus({
        otsikko: "Lukion oppimäärä 2019 –",
        tila: "Läsnä",
        maksuttomuus: ["Ei"],
        toimipiste: "Jyväskylän normaalikoulu",
        ryhmä: "AH",
        alkamispäivä: "1.8.2019",
      })
    )
  })

  it("Näyttää detaljisivun maksuttomuuden oppijasta, jolla ei ole oppivelvollisuuden suorittamiseen kelpaavaa opiskeluoikeutta", async () => {
    await loginAs(
      eiOppivelvollisuudenSuorittamiseenKelpaaviaOpiskeluoikeuksiaPath,
      "valpas-maksuttomuus-hki"
    )
    await mainHeadingEquals(
      "Ei-oppivelvollisuuden-suorittamiseen-kelpaavia-opiskeluoikeuksia Valpas (061005A671V)"
    )
  })

  it("Näyttää detaljisivun hetuttomasta oppijasta", async () => {
    await loginAs(hetutonPath, "valpas-maksuttomuus-hki")
    await mainHeadingEquals("Hetuton Valpas")
  })

  it("Ei näytä detaljisivua kuntakäyttäjälle lukio-oppijasta oppivelvollisuuden päätyttyä", async () => {
    allowNetworkError("/valpas/api/oppija/", FORBIDDEN)
    await loginAs(lukioOpiskelijaPath, "valpas-helsinki")

    await resetMockData("2022-08-10")
    await goToLocation(lukioOpiskelijaPath)

    await mainHeadingEquals("Oppijan tiedot")
    await secondaryHeadingEquals(
      "Oppijaa ei löydy tunnuksella 1.2.246.562.24.00000000004"
    )
    await expectEiKuntailmoituksiaNotVisible()
  })

  it("Ei näytä detaljisivua suorittamisen valvojalle lukio-oppijasta oppivelvollisuuden päätyttyä", async () => {
    allowNetworkError("/valpas/api/oppija/", FORBIDDEN)
    await loginAs(lukioOpiskelijaPath, "valpas-pelkkä-suorittaminen")

    await resetMockData("2022-08-10")
    await goToLocation(lukioOpiskelijaPath)

    await mainHeadingEquals("Oppijan tiedot")
    await secondaryHeadingEquals(
      "Oppijaa ei löydy tunnuksella 1.2.246.562.24.00000000004"
    )
    await expectEiKuntailmoituksiaNotVisible()
  })

  it("Näyttää oppijan oppivelvollisuuden määräaikaisen keskeytyksen", async () => {
    await loginAs(
      oppivelvollisuusKeskeytettyMääräajaksiPath,
      "valpas-jkl-normaali"
    )
    await mainHeadingEquals(
      "Oppivelvollisuus-keskeytetty-määräajaksi Valpas (181005A1560)"
    )
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "17.10.2023 asti",
        oppivelvollisuudenKeskeytykset: ["1.3.2021 – 30.9.2021"],
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOppivelvollisuudenKeskeytys("1.3.2021 – 30.9.2021"),
        historiaOppivelvollisuudenKeskeytys("1.1.2020 – 30.1.2020"),
        historiaOpintoOikeus({
          otsikko: "Perusopetus 2012 –",
          tila: "Läsnä",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9C",
          alkamispäivä: "15.8.2012",
        })
      )
    )
  })

  it("Näyttää oppijan oppivelvollisuuden keskeytyksen toistaiseksi", async () => {
    await loginAs(
      oppivelvollisuusKeskeytettyToistaiseksiPath,
      "valpas-jkl-normaali"
    )
    await mainHeadingEquals(
      "Oppivelvollisuus-keskeytetty-toistaiseksi Valpas (150905A1823)"
    )
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "Keskeytetty toistaiseksi 1.1.2021 alkaen",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOppivelvollisuudenKeskeytysToistaiseksi("1.1.2021"),
        historiaOpintoOikeus({
          otsikko: "Perusopetus 2012 –",
          tila: "Läsnä",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9C",
          alkamispäivä: "15.8.2012",
        })
      )
    )
  })

  it("Näyttää oppijan oppivelvollisuuden umpeutuneen määräaikaisen keskeytyksen oikein", async () => {
    await loginAs(
      oppivelvollisuusKeskeytettyMääräajaksiPath,
      "valpas-jkl-normaali"
    )

    await resetMockData("2022-10-01")
    await goToLocation(oppivelvollisuusKeskeytettyMääräajaksiPath)

    await mainHeadingEquals(
      "Oppivelvollisuus-keskeytetty-määräajaksi Valpas (181005A1560)"
    )
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "17.10.2023 asti",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOppivelvollisuudenKeskeytys("1.3.2021 – 30.9.2021"),
        historiaOppivelvollisuudenKeskeytys("1.1.2020 – 30.1.2020"),
        historiaOpintoOikeus({
          otsikko: "Perusopetus 2012 –",
          tila: "Läsnä",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9C",
          alkamispäivä: "15.8.2012",
        })
      )
    )
  })

  it("Oppivelvollisuuden keskeytyksen lisäys toimii oikein", async () => {
    await loginAs(oppivelvollisuusKeskeytettyMääräajaksiPath, "valpas-helsinki")

    await resetMockData("2022-11-11")
    await goToLocation(oppivelvollisuusKeskeytettyMääräajaksiPath)
    await mainHeadingEquals(
      "Oppivelvollisuus-keskeytetty-määräajaksi Valpas (181005A1560)"
    )

    // Avaa ov-keskeytysmodaali
    await clickElement("#ovkeskeytys-btn")
    await textEventuallyEquals(
      ".modal__title",
      "Oppivelvollisuuden keskeytyksen lisäys"
    )
    await textEventuallyEquals(
      ".modal__container .heading--secondary",
      "Oppivelvollisuus-keskeytetty-määräajaksi Valpas (181005A1560)"
    )

    // Valitse "Oppivelvollisuus keskeytetään toistaiseksi", säilytä alkupäivänä nykyinen päivä, hyväksy ehto
    await clickElement(
      ".ovkeskeytys__option:nth-child(2) .radiobutton__container"
    )
    await clickElement(".ovkeskeytys__option:nth-child(2) .checkbox__labeltext")
    await clickElement("#ovkeskeytys-submit")

    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuudenKeskeytykset: [`toistaiseksi 11.11.2022 alkaen`],
        maksuttomuusoikeus: "31.12.2025 asti",
        oppivelvollisuudenKeskeytysBtn: true,
        kuntailmoitusBtn: true,
        merkitseVapautusBtn: true,
      })
    )
  })

  it("Oppivelvollisuuden keskeytyksen muokkaus toimii oikein", async () => {
    await loginAs(
      oppivelvollisuusKeskeytettyHelsinkiläinenPath,
      "valpas-helsinki",
      true,
      "2021-09-05"
    )

    await mainHeadingEquals(
      "Oppivelvollisuus-keskeytetty-ei-opiskele Valpas (011005A115P)"
    )

    // Avaa ov-keskeytysmodaali
    await clickElement(".oppijaview__editkeskeytysbtn")
    await textEventuallyEquals(
      ".modal__title",
      "Oppivelvollisuuden keskeytyksen muokkaus"
    )
    await textEventuallyEquals(
      ".modal__container .heading--secondary",
      "Oppivelvollisuus-keskeytetty-ei-opiskele Valpas (011005A115P)"
    )

    // Valitse "Oppivelvollisuus keskeytetään toistaiseksi", säilytä alkupäivänä nykyinen päivä, hyväksy ehto
    await clickElement(
      ".ovkeskeytys__option:nth-child(2) .radiobutton__container"
    )
    await clickElement(".ovkeskeytys__option:nth-child(2) .checkbox__labeltext")
    await clickElement("#ovkeskeytys-submit-edit")

    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Ei opiskelupaikkaa",
        oppivelvollisuudenKeskeytykset: ["toistaiseksi 16.8.2021 alkaen"],
        maksuttomuusoikeus: "31.12.2025 asti",
        oppivelvollisuudenKeskeytysBtn: true,
        kuntailmoitusBtn: true,
        merkitseVapautusBtn: true,
      })
    )
  })

  it("Oppivelvollisuuden keskeytystä ei voi asettaa alkavaksi ennen 1.8.2021", async () => {
    allowNetworkError("/valpas/api/oppija/ovkeskeytys", BAD_REQUEST)

    await loginAs(
      oppivelvollisuusKeskeytettyHelsinkiläinenPath,
      "valpas-helsinki",
      true,
      "2021-07-31"
    )

    await mainHeadingEquals(
      "Oppivelvollisuus-keskeytetty-ei-opiskele Valpas (011005A115P)"
    )

    // Avaa ov-keskeytysmodaali
    await clickElement("#ovkeskeytys-btn")
    await textEventuallyEquals(
      ".modal__title",
      "Oppivelvollisuuden keskeytyksen lisäys"
    )
    await textEventuallyEquals(
      ".modal__container .heading--secondary",
      "Oppivelvollisuus-keskeytetty-ei-opiskele Valpas (011005A115P)"
    )

    // Valitse "Oppivelvollisuus keskeytetään toistaiseksi", säilytä alkupäivänä nykyinen päivä, hyväksy ehto
    await clickElement(
      ".ovkeskeytys__option:nth-child(2) .radiobutton__container"
    )
    await clickElement(".ovkeskeytys__option:nth-child(2) .checkbox__labeltext")
    await clickElement("#ovkeskeytys-submit")

    await textEventuallyEquals(
      ".modal__container .error",
      "Alkamispäivä ei voi olla ennen 1.8.2021"
    )
  })

  it("Oppivelvollisuuden keskeytyksen poisto toimii oikein", async () => {
    await loginAs(
      oppivelvollisuusKeskeytettyHelsinkiläinenPath,
      "valpas-helsinki",
      true,
      "2021-09-05"
    )

    await mainHeadingEquals(
      "Oppivelvollisuus-keskeytetty-ei-opiskele Valpas (011005A115P)"
    )

    // Avaa ov-keskeytysmodaali
    await clickElement(".oppijaview__editkeskeytysbtn")
    await textEventuallyEquals(
      ".modal__title",
      "Oppivelvollisuuden keskeytyksen muokkaus"
    )
    await textEventuallyEquals(
      ".modal__container .heading--secondary",
      "Oppivelvollisuus-keskeytetty-ei-opiskele Valpas (011005A115P)"
    )

    // Valitse "Oppivelvollisuus keskeytetään toistaiseksi", säilytä alkupäivänä nykyinen päivä, hyväksy ehto
    await clickElement("#ovkeskeytys-delete")
    await acceptConfirmation()

    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Ei opiskelupaikkaa",
        oppivelvollisuus: "30.9.2023 asti",
        maksuttomuusoikeus: "31.12.2025 asti",
        oppivelvollisuudenKeskeytysBtn: true,
        kuntailmoitusBtn: true,
        merkitseVapautusBtn: true,
      })
    )
  })

  it("Näytä väliaikaisesti keskeytynyt opiskeluoikeus", async () => {
    await loginAs(
      opiskeluoikeusKeskeytettyMääräajaksiPath,
      "valpas-jkl-yliopisto-suorittaminen"
    )

    await resetMockData("2021-08-15")

    await opiskeluhistoriaEquals(
      historiaOpintoOikeus({
        otsikko: "Lukion oppimäärä 2021 –",
        tila: "Väliaikaisesti keskeytynyt 2.8.2021",
        maksuttomuus: ["Ei"],
        toimipiste: "Jyväskylän normaalikoulu",
        ryhmä: "AH",
        alkamispäivä: "1.8.2021",
      })
    )
  })

  it("Näytä lomailevan ammattikoululaisen opiskeluoikeus", async () => {
    await loginAs(opiskeluoikeusLomaPath, "valpas-pelkkä-suorittaminen-amis")

    await resetMockData("2021-08-15")

    await opiskeluhistoriaEquals(
      historiaOpintoOikeus({
        otsikko: "Ammatillinen tutkinto 2021 –",
        tila: "Loma",
        maksuttomuus: ["1.8.2021– maksuton"],
        toimipiste:
          "Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka",
        alkamispäivä: "1.8.2021",
      })
    )
  })

  it("Näytä koulutustyyppi oikein", async () => {
    await loginAs(opiskeluoikeusValmaPath, "valpas-pelkkä-suorittaminen-amis")

    await resetMockData("2021-08-15")

    await opiskeluhistoriaEquals(
      historiaOpintoOikeus({
        otsikko: "VALMA 2012 –",
        tila: "Läsnä",
        maksuttomuus: ["Ei"],
        toimipiste: "Stadin ammatti- ja aikuisopisto",
        alkamispäivä: "1.9.2012",
      })
    )
  })

  it("Näyttää maksuttomuuden pidennyksen", async () => {
    await loginAs(maksuttomuuttaPidennettyPath, "valpas-monta")
    await mainHeadingEquals("Maksuttomuutta-pidennetty Valpas (070604A200U)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000127")
    await opiskeluhistoriaEquals(
      historiaOpintoOikeus({
        otsikko: "Ammatillinen tutkinto 2021 –",
        tila: "Läsnä",
        maksuttomuus: [
          "Oikeutta maksuttomuuteen pidennetty 1.9.2021–31.12.2023",
          "1.9.2021– maksuton",
        ],
        toimipiste: "Omnia Koulutus, Arbetarinstitut",
        alkamispäivä: "1.9.2021",
      })
    )
  })

  it("Näyttää perusopetukseen valmistavan opetuksen opiskeluhistoriassa", async () => {
    await loginAs(perusopetukseenValmistautuvaPath, "valpas-jkl-normaali", true)
    await mainHeadingEquals(
      "Perusopetukseen-valmistautuva Valpas (151011A1403)"
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000131")
    await opiskeluhistoriaEquals(
      historiaOpintoOikeus({
        otsikko: "Perusopetukseen valmistava opetus 2021 –",
        tila: "Läsnä",
        toimipiste: "Jyväskylän normaalikoulu",
        alkamispäivä: "1.5.2021",
      })
    )
  })

  it("Näyttää oppijan kaikki kuntailmoitukset", async () => {
    await loginAs(montaKuntailmoitustaPath, "valpas-monta", true, "2021-12-01")
    await mainHeadingEquals(
      "LukionAloittanutJaLopettanut-ilmo Valpas (050405A249S)"
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaVastuuilmoitus({
          päivämäärä: "30.11.2021",
          ilmoittaja: "Jyväskylän normaalikoulu",
          tahoJolleIlmoitettu: "Helsinki",
        }),
        historiaVastuuilmoitus({
          päivämäärä: "20.9.2021",
          ilmoittaja: "Jyväskylän normaalikoulu",
          tahoJolleIlmoitettu: "Pyhtää",
        }),
        historiaVastuuilmoitus({
          päivämäärä: "15.9.2021",
          ilmoittaja: "Jyväskylän normaalikoulu",
          tahoJolleIlmoitettu: "Helsinki",
        }),
        historiaOpintoOikeus({
          otsikko: "Lukion oppimäärä 2021 – 2021",
          tila: "Eronnut",
          maksuttomuus: ["15.8.2021– maksuton"],
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "AH",
          alkamispäivä: "15.8.2021",
          päättymispäivä: "19.9.2021",
        }),
        historiaVastuuilmoitus({
          päivämäärä: "15.6.2021",
          ilmoittaja: "Jyväskylän normaalikoulu",
          tahoJolleIlmoitettu: "Pyhtää",
        }),
        historiaOpintoOikeus({
          otsikko: "Perusopetus 2012 – 2021",
          tila: "Valmistunut",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9C",
          alkamispäivä: "15.8.2012",
          päättymispäivä: "30.5.2021",
        })
      )
    )
  })

  it("Näyttää preIB oppijan tiedot", async () => {
    await loginAs(preIBdOppijaPath, "valpas-monta", true)
    await mainHeadingEquals("SuorittaaPreIB Valpas (190704A574E)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000135")
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoOikeus({
          otsikko: "IB 2021 –",
          tila: "Läsnä",
          maksuttomuus: ["1.6.2021– maksuton"],
          toimipiste: "Jyväskylän normaalikoulu",
          alkamispäivä: "1.6.2021",
        }),
        historiaOpintoOikeus({
          otsikko: "Perusopetus 2012 – 2021",
          tila: "Valmistunut",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9C",
          alkamispäivä: "15.8.2012",
          päättymispäivä: "30.5.2021",
        })
      )
    )
  })

  it("Näyttää esiopetusoppijan tiedot", async () => {
    await loginAs(pelkkäEsiopetusPath, "valpas-monta", true)
    await mainHeadingEquals("Esikoululainen Valpas (270615A6481)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000126")
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoOikeus({
          otsikko: "Esiopetuksen suoritus 2021 –",
          tila: "Läsnä",
          toimipiste: "Jyväskylän normaalikoulu",
          alkamispäivä: "13.8.2021",
        })
      )
    )
  })

  it("Näyttää ohjetekstin, jos oppijalle ei löydy opintohistoriaa", async () => {
    await loginAs(
      eiKoskessaOppivelvollinenJollaKeskeytyksiäJaIlmoituksiaPath,
      "valpas-monta"
    )

    await mainHeadingEquals(
      "Kosketon-keskeytyksiä-ilmoituksia Valpas (260705A1119)"
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000144")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Ei opiskelupaikkaa",
        oppivelvollisuudenKeskeytykset: ["toistaiseksi 1.9.2021 alkaen"],
        maksuttomuusoikeus: "31.12.2025 asti",
        oppivelvollisuudenKeskeytysBtn: true,
        merkitseVapautusBtn: true,
      })
    )
    await contentEventuallyEquals(
      testId("ei-opiskeluoikeushistoria-opintoja-text"),
      "Oppijalle ei löytynyt opiskeluhistoriaa"
    )
  })

  it("Oppivelvollisuuden keskeytys toimii oppijalle, jota ei löydy Koskesta", async () => {
    await loginAs(
      eiKoskessaOppivelvollinenJollaKeskeytyksiäJaIlmoituksiaPath,
      "valpas-monta"
    )

    await mainHeadingEquals(
      "Kosketon-keskeytyksiä-ilmoituksia Valpas (260705A1119)"
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000144")

    // Avaa ov-keskeytysmodaali
    await clickElement(testId("ovkeskeytys-btn"))
    await textEventuallyEquals(
      testId("ovkeskeytys-modal__container-header-title"),
      "Oppivelvollisuuden keskeytyksen lisäys"
    )

    await textEventuallyEquals(
      testId("ovkeskeytys-secondary-heading"),
      "Kosketon-keskeytyksiä-ilmoituksia Valpas (260705A1119)"
    )

    // Valitse "Oppivelvollisuus keskeytetään toistaiseksi", säilytä alkupäivänä nykyinen päivä, hyväksy ehto
    await clickElement(testId("ovkeskeytys-toistaiseksi-option"))
    await clickElement(testId("ovkeskeytys-toistaiseksi-vahvistus"))
    await clickElement(testId("ovkeskeytys-submit"))

    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Ei opiskelupaikkaa",
        oppivelvollisuudenKeskeytykset: [
          "toistaiseksi 5.9.2021 alkaen",
          "toistaiseksi 1.9.2021 alkaen",
        ],
        maksuttomuusoikeus: "31.12.2025 asti",
        oppivelvollisuudenKeskeytysBtn: true,
        merkitseVapautusBtn: true,
      })
    )
  })
})

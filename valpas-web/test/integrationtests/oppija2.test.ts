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
  },
)
const oppivelvollisuusKeskeytettyToistaiseksiPath = oppijaPath.href(
  "/virkailija",
  {
    oppijaOid: "1.2.246.562.24.00000000057",
  },
)
const eiOppivelvollisuudenSuorittamiseenKelpaaviaOpiskeluoikeuksiaPath =
  oppijaPath.href("/virkailija", {
    oppijaOid: "1.2.246.562.24.00000000058",
  })
const hetutonPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000059",
})

const opiskeluoikeusKeskeytettyMääräajaksiPath = oppijaPath.href(
  "/virkailija",
  {
    oppijaOid: "1.2.246.562.24.00000000081",
  },
)

const opiskeluoikeusLomaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000082",
})

const opiskeluoikeusValmaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000062",
})

const maksuttomuuttaPidennettyPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000131",
})

const perusopetukseenValmistautuvaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000135",
})

const montaKuntailmoitustaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000041",
})

const preIBdOppijaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000139",
})

const oppivelvollisuusKeskeytettyHelsinkiläinenPath = oppijaPath.href(
  "/virkailija",
  {
    oppijaOid: "1.2.246.562.24.00000000134",
  },
)

const eiKoskessaOppivelvollinenJollaKeskeytyksiäJaIlmoituksiaPath =
  oppijaPath.href("/virkailija", {
    oppijaOid: "1.2.246.562.24.00000000148",
  })

const pelkkäEsiopetusPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000130",
})

const pelkkäESHNurseryPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000173",
})

const eshKeskenEbTutkinnonAloittanutPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000179",
})

const lukionAineopintojaJaAmmatillisiaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000174",
})

const yoTutkintoJaAmmatillisiaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000175",
})

const taiteenPerusopetusPäättynytPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000180",
})

const maahanmuuttajaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000181",
})

const ammattitutkintoYoTutkinnonJalkeenPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000182",
})

const lukioOpinnotAmmattitutkinnonJalkeenPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000183",
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
        opiskelutilanne: "Kyllä",
        oppivelvollisuus: "6.5.2022 asti",
        maksuttomuusoikeus: "31.12.2024 asti",
        kuntailmoitusBtn: true,
      }),
    )
    await opiskeluhistoriaEquals(
      historiaOpintoOikeus({
        otsikko: "Lukion oppimäärä 2019 –",
        tila: "Läsnä",
        maksuttomuus: ["Ei"],
        toimipiste: "Jyväskylän normaalikoulu",
        ryhmä: "AH",
        alkamispäivä: "1.8.2019",
      }),
    )
  })

  it("Näyttää detaljisivun maksuttomuuden oppijasta, jolla ei ole oppivelvollisuuden suorittamiseen kelpaavaa opiskeluoikeutta", async () => {
    await loginAs(
      eiOppivelvollisuudenSuorittamiseenKelpaaviaOpiskeluoikeuksiaPath,
      "valpas-maksuttomuus-hki",
    )
    await mainHeadingEquals(
      "Ei-oppivelvollisuuden-suorittamiseen-yksinään-kelpaavia-opiskeluoikeuksia Valpas (061005A671V)",
    )
  })

  it("Näyttää detaljisivun hetuttomasta oppijasta", async () => {
    await loginAs(hetutonPath, "valpas-maksuttomuus-hki")
    await mainHeadingEquals("Hetuton Valpas")
  })

  it("Näyttää oppijan oppivelvollisuuden määräaikaisen keskeytyksen", async () => {
    await loginAs(
      oppivelvollisuusKeskeytettyMääräajaksiPath,
      "valpas-jkl-normaali",
    )
    await mainHeadingEquals(
      "Oppivelvollisuus-keskeytetty-määräajaksi Valpas (181005A1560)",
    )
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Kyllä",
        oppivelvollisuus: "17.10.2023 asti",
        oppivelvollisuudenKeskeytykset: [
          "1.9.2021 – 30.9.2021",
          "1.1.2020 – 30.1.2020",
        ],
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      }),
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOppivelvollisuudenKeskeytys("1.9.2021 – 30.9.2021"),
        historiaOppivelvollisuudenKeskeytys("1.1.2020 – 30.1.2020"),
        historiaOpintoOikeus({
          otsikko: "Perusopetus 2012 –",
          tila: "Läsnä",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9C",
          alkamispäivä: "15.8.2012",
        }),
      ),
    )
  })

  it("Näyttää oppijan oppivelvollisuuden keskeytyksen toistaiseksi", async () => {
    await loginAs(
      oppivelvollisuusKeskeytettyToistaiseksiPath,
      "valpas-jkl-normaali",
    )
    await mainHeadingEquals(
      "Oppivelvollisuus-keskeytetty-toistaiseksi Valpas (150905A1823)",
    )
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Kyllä",
        oppivelvollisuus: "Keskeytetty toistaiseksi 1.1.2021 alkaen",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      }),
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
        }),
      ),
    )
  })

  it("Oppivelvollisuuden keskeytyksen muokkaus toimii oikein", async () => {
    await loginAs(
      oppivelvollisuusKeskeytettyHelsinkiläinenPath,
      "valpas-helsinki",
      true,
      "2021-09-05",
    )

    await mainHeadingEquals(
      "Oppivelvollisuus-keskeytetty-ei-opiskele Valpas (011005A115P)",
    )

    // Avaa ov-keskeytysmodaali
    await clickElement(".oppijaview__editkeskeytysbtn")
    await textEventuallyEquals(
      ".modal__title",
      "Oppivelvollisuuden keskeytyksen muokkaus",
    )
    await textEventuallyEquals(
      ".modal__container .heading--secondary",
      "Oppivelvollisuus-keskeytetty-ei-opiskele Valpas (011005A115P)",
    )

    // Valitse "Oppivelvollisuus keskeytetään toistaiseksi", säilytä alkupäivänä nykyinen päivä, hyväksy ehto
    await clickElement(
      ".ovkeskeytys__option:nth-child(2) .radiobutton__container",
    )
    await clickElement(".ovkeskeytys__option:nth-child(2) .checkbox__labeltext")
    await clickElement("#ovkeskeytys-submit-edit")

    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Ei",
        oppivelvollisuudenKeskeytykset: ["toistaiseksi 16.8.2021 alkaen"],
        maksuttomuusoikeus: "31.12.2025 asti",
        oppivelvollisuudenKeskeytysBtn: true,
        kuntailmoitusBtn: true,
        merkitseVapautusBtn: true,
      }),
    )
  })

  it("Oppivelvollisuuden keskeytystä ei voi asettaa alkavaksi ennen 1.8.2021", async () => {
    allowNetworkError("/valpas/api/oppija/ovkeskeytys", BAD_REQUEST)

    await loginAs(
      oppivelvollisuusKeskeytettyHelsinkiläinenPath,
      "valpas-helsinki",
      true,
      "2021-07-31",
    )

    await mainHeadingEquals(
      "Oppivelvollisuus-keskeytetty-ei-opiskele Valpas (011005A115P)",
    )

    // Avaa ov-keskeytysmodaali
    await clickElement("#ovkeskeytys-btn")
    await textEventuallyEquals(".modal__title", "Oppivelvollisuuden keskeytys")
    await textEventuallyEquals(
      ".modal__container .heading--secondary",
      "Oppivelvollisuus-keskeytetty-ei-opiskele Valpas (011005A115P)",
    )

    // Valitse "Oppivelvollisuus keskeytetään toistaiseksi", säilytä alkupäivänä nykyinen päivä, hyväksy ehto
    await clickElement(
      ".ovkeskeytys__option:nth-child(2) .radiobutton__container",
    )
    await clickElement(".ovkeskeytys__option:nth-child(2) .checkbox__labeltext")
    await clickElement("#ovkeskeytys-submit")

    await textEventuallyEquals(
      ".modal__container .error",
      "Alkamispäivä ei voi olla ennen 1.8.2021",
    )
  })

  it("Oppivelvollisuuden keskeytyksen poisto toimii oikein", async () => {
    await loginAs(
      oppivelvollisuusKeskeytettyHelsinkiläinenPath,
      "valpas-helsinki",
      true,
      "2021-09-05",
    )

    await mainHeadingEquals(
      "Oppivelvollisuus-keskeytetty-ei-opiskele Valpas (011005A115P)",
    )

    // Avaa ov-keskeytysmodaali
    await clickElement(".oppijaview__editkeskeytysbtn")
    await textEventuallyEquals(
      ".modal__title",
      "Oppivelvollisuuden keskeytyksen muokkaus",
    )
    await textEventuallyEquals(
      ".modal__container .heading--secondary",
      "Oppivelvollisuus-keskeytetty-ei-opiskele Valpas (011005A115P)",
    )

    // Valitse "Oppivelvollisuus keskeytetään toistaiseksi", säilytä alkupäivänä nykyinen päivä, hyväksy ehto
    await clickElement("#ovkeskeytys-delete")
    await acceptConfirmation()

    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Ei",
        oppivelvollisuus: "30.9.2023 asti",
        maksuttomuusoikeus: "31.12.2025 asti",
        oppivelvollisuudenKeskeytysBtn: true,
        kuntailmoitusBtn: true,
        merkitseVapautusBtn: true,
      }),
    )
  })

  it("Näyttää maksuttomuuden pidennyksen", async () => {
    await loginAs(maksuttomuuttaPidennettyPath, "valpas-monta")
    await mainHeadingEquals("Maksuttomuutta-pidennetty Valpas (070604A200U)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000131")

    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Kyllä",
        oppivelvollisuus: "6.6.2022 asti",
        maksuttomuusoikeus: "30.6.2025 asti",
        oppivelvollisuudenKeskeytysBtn: true,
        kuntailmoitusBtn: true,
        merkitseVapautusBtn: true,
      }),
    )

    await opiskeluhistoriaEquals(
      historiaOpintoOikeus({
        otsikko: "Ammatillinen tutkinto 2021 –",
        tila: "Läsnä",
        maksuttomuus: [
          "Oikeutta maksuttomuuteen pidennetty 1.1.2025–31.5.2025",
          "Oikeutta maksuttomuuteen pidennetty 1.6.2025–30.6.2025",
          "1.9.2021– maksuton",
        ],
        toimipiste: "Omnia Koulutus, Arbetarinstitut",
        alkamispäivä: "1.9.2021",
      }),
    )
  })

  it("Näyttää perusopetukseen valmistavan opetuksen opiskeluhistoriassa", async () => {
    await loginAs(perusopetukseenValmistautuvaPath, "valpas-jkl-normaali", true)
    await mainHeadingEquals(
      "Perusopetukseen-valmistautuva Valpas (151011A1403)",
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000135")
    await opiskeluhistoriaEquals(
      historiaOpintoOikeus({
        otsikko: "Perusopetukseen valmistava opetus 2021 –",
        tila: "Läsnä",
        toimipiste: "Jyväskylän normaalikoulu",
        alkamispäivä: "1.5.2021",
      }),
    )
  })

  it("Näyttää oppijan kaikki kuntailmoitukset", async () => {
    await loginAs(montaKuntailmoitustaPath, "valpas-monta", true, "2021-12-01")
    await mainHeadingEquals(
      "LukionAloittanutJaLopettanut-ilmo Valpas (050405A249S)",
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
        }),
      ),
    )
  })

  it("Näyttää preIB oppijan tiedot", async () => {
    await loginAs(preIBdOppijaPath, "valpas-monta", true)
    await mainHeadingEquals("SuorittaaPreIB Valpas (190704A574E)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000139")
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
        }),
      ),
    )
  })

  it("Näyttää esiopetusoppijan tiedot", async () => {
    await loginAs(pelkkäEsiopetusPath, "valpas-monta", true)
    await mainHeadingEquals("Esikoululainen Valpas (270615A6481)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000130")
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoOikeus({
          otsikko: "Esiopetuksen suoritus 2021 –",
          tila: "Läsnä",
          toimipiste: "Jyväskylän normaalikoulu",
          alkamispäivä: "13.8.2021",
        }),
      ),
    )
  })

  it("Näyttää European School of Helsinki nursery-oppijan tiedot", async () => {
    await loginAs(pelkkäESHNurseryPath, "valpas-monta", true)
    await mainHeadingEquals("ESH-nurseryssä Valpas (070614A452J)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000173")
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoOikeus({
          otsikko: "European School of Helsinki 2021 –",
          tila: "Läsnä",
          toimipiste: "Helsingin eurooppalainen koulu",
          alkamispäivä: "1.8.2021",
          ryhmä: "N2A",
          maksuttomuus: ["Ei"],
        }),
      ),
    )
  })

  it("Näyttää Lukion aineopintoja ja ammattiopintoja opiskelevan oppijan tiedot, mukaanlukien aineopinnot", async () => {
    await loginAs(lukionAineopintojaJaAmmatillisiaPath, "valpas-monta", true)
    await mainHeadingEquals(
      "Lukion-aineopinnot-ja-ammatillisia Valpas (121005A797T)",
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000174")
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoOikeus({
          otsikko: "Lukion aineopinnot 2021 –",
          tila: "Läsnä",
          toimipiste: "Jyväskylän normaalikoulu",
          alkamispäivä: "15.8.2021",
          maksuttomuus: ["Ei"],
        }),
        historiaOpintoOikeus({
          otsikko: "Ammatillinen tutkinto 2012 –",
          tila: "Läsnä",
          maksuttomuus: ["Ei"],
          toimipiste:
            "Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka",
          alkamispäivä: "1.9.2012",
        }),
      ),
    )
  })

  it("Näyttää ohjetekstin, jos oppijalle ei löydy opintohistoriaa", async () => {
    await loginAs(
      eiKoskessaOppivelvollinenJollaKeskeytyksiäJaIlmoituksiaPath,
      "valpas-monta",
    )

    await mainHeadingEquals(
      "Kosketon-keskeytyksiä-ilmoituksia Valpas (260705A1119)",
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000148")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Ei",
        oppivelvollisuudenKeskeytykset: [
          "toistaiseksi 1.9.2021 alkaen",
          "1.1.2019 – 1.12.2019",
        ],
        maksuttomuusoikeus: "31.12.2025 asti",
        oppivelvollisuudenKeskeytysBtn: true,
        merkitseVapautusBtn: true,
      }),
    )
    await contentEventuallyEquals(
      testId("ei-opiskeluoikeushistoria-opintoja-text"),
      "Oppijalle ei löytynyt oppivelvollisuuden suorittamiseen kelpaavaa opiskeluhistoriaa",
    )
  })

  it("Oppivelvollisuuden keskeytys toimii oppijalle, jota ei löydy Koskesta", async () => {
    await loginAs(
      eiKoskessaOppivelvollinenJollaKeskeytyksiäJaIlmoituksiaPath,
      "valpas-monta",
    )

    await mainHeadingEquals(
      "Kosketon-keskeytyksiä-ilmoituksia Valpas (260705A1119)",
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000148")

    // Avaa ov-keskeytysmodaali
    await clickElement(testId("ovkeskeytys-btn"))
    await textEventuallyEquals(
      testId("ovkeskeytys-modal__container-header-title"),
      "Oppivelvollisuuden keskeytys",
    )

    await textEventuallyEquals(
      testId("ovkeskeytys-secondary-heading"),
      "Kosketon-keskeytyksiä-ilmoituksia Valpas (260705A1119)",
    )

    // Valitse "Oppivelvollisuus keskeytetään toistaiseksi", säilytä alkupäivänä nykyinen päivä, hyväksy ehto
    await clickElement(testId("ovkeskeytys-toistaiseksi-option"))
    await clickElement(testId("ovkeskeytys-toistaiseksi-vahvistus"))
    await clickElement(testId("ovkeskeytys-submit"))

    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Ei",
        oppivelvollisuudenKeskeytykset: [
          "toistaiseksi 5.9.2021 alkaen",
          "toistaiseksi 1.9.2021 alkaen",
          "1.1.2019 – 1.12.2019",
        ],
        maksuttomuusoikeus: "31.12.2025 asti",
        oppivelvollisuudenKeskeytysBtn: true,
        merkitseVapautusBtn: true,
      }),
    )
  })

  it("Näyttää ammattiopintoja opiskelevan oppijan, jolla YO-tutkinto, tiedot, mukaanlukien YO-tutkinnon", async () => {
    await loginAs(yoTutkintoJaAmmatillisiaPath, "valpas-monta", true)
    await mainHeadingEquals("Amis-ja-YO Valpas (300805A756F)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000175")
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoOikeus({
          otsikko: "Ylioppilastutkinto 2021",
          tila: "Valmistunut",
          toimipiste: "Ylioppilastutkintolautakunta",
          päättymispäivä: "5.9.2021",
        }),
        historiaOpintoOikeus({
          otsikko: "Ammatillinen tutkinto 2021 – 2021",
          tila: "Eronnut",
          maksuttomuus: ["1.8.2021– maksuton"],
          toimipiste:
            "Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka",
          alkamispäivä: "1.8.2021",
          päättymispäivä: "2.9.2021",
        }),
        historiaOpintoOikeus({
          otsikko: "Perusopetus 2012 – 2021",
          tila: "Valmistunut",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9C",
          alkamispäivä: "14.8.2012",
          päättymispäivä: "29.5.2021",
        }),
      ),
    )
  })

  describe("Kun tarkastelupäivä vaihdetaan", () => {
    it("Näyttää EB-tutkintoa opiskelevan tiedot", async () => {
      await loginAs(eshKeskenEbTutkinnonAloittanutPath, "valpas-monta", true)

      await resetMockData("2023-09-05")
      await goToLocation(eshKeskenEbTutkinnonAloittanutPath)

      await mainHeadingEquals(
        "ESH-kesken-EB-tutkinnon-aloittanut Valpas (021110A1065)",
      )
      await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000179")
      await opiskeluhistoriaEquals(
        merge(
          historiaOpintoOikeus({
            otsikko: "European Baccalaureate 2023 –",
            tila: "Läsnä",
            toimipiste: "Helsingin eurooppalainen koulu",
            alkamispäivä: "15.6.2023",
          }),
          historiaOpintoOikeus({
            otsikko: "European School of Helsinki 2022 –",
            tila: "Läsnä",
            toimipiste: "Helsingin eurooppalainen koulu",
            alkamispäivä: "1.9.2022",
            ryhmä: "S7A",
            maksuttomuus: ["Ei"],
          }),
        ),
      )
    })

    it("Ei näytä detaljisivua kuntakäyttäjälle lukio-oppijasta oppivelvollisuuden päätyttyä", async () => {
      allowNetworkError("/valpas/api/oppija/", FORBIDDEN)
      await loginAs(lukioOpiskelijaPath, "valpas-helsinki")

      await resetMockData("2022-08-10")
      await goToLocation(lukioOpiskelijaPath)

      await mainHeadingEquals("Oppijan tiedot")
      await secondaryHeadingEquals(
        "Oppijaa ei löydy tunnuksella 1.2.246.562.24.00000000004",
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
        "Oppijaa ei löydy tunnuksella 1.2.246.562.24.00000000004",
      )
      await expectEiKuntailmoituksiaNotVisible()
    })

    it("Näyttää oppijan oppivelvollisuuden umpeutuneen määräaikaisen keskeytyksen oikein", async () => {
      await loginAs(
        oppivelvollisuusKeskeytettyMääräajaksiPath,
        "valpas-jkl-normaali",
      )

      await resetMockData("2022-10-01")
      await goToLocation(oppivelvollisuusKeskeytettyMääräajaksiPath)

      await mainHeadingEquals(
        "Oppivelvollisuus-keskeytetty-määräajaksi Valpas (181005A1560)",
      )
      await oppivelvollisuustiedotEquals(
        oppivelvollisuustiedot({
          opiskelutilanne: "Kyllä",
          oppivelvollisuus: "17.10.2023 asti",
          oppivelvollisuudenKeskeytykset: [
            "1.9.2021 – 30.9.2021",
            "1.1.2020 – 30.1.2020",
          ],
          maksuttomuusoikeus: "31.12.2025 asti",
          kuntailmoitusBtn: true,
        }),
      )
      await opiskeluhistoriaEquals(
        merge(
          historiaOppivelvollisuudenKeskeytys("1.9.2021 – 30.9.2021"),
          historiaOppivelvollisuudenKeskeytys("1.1.2020 – 30.1.2020"),
          historiaOpintoOikeus({
            otsikko: "Perusopetus 2012 –",
            tila: "Läsnä",
            toimipiste: "Jyväskylän normaalikoulu",
            ryhmä: "9C",
            alkamispäivä: "15.8.2012",
          }),
        ),
      )
    })

    it("Oppivelvollisuuden keskeytyksen lisäys toimii oikein", async () => {
      await loginAs(
        oppivelvollisuusKeskeytettyMääräajaksiPath,
        "valpas-helsinki",
      )

      await resetMockData("2022-11-11")
      await goToLocation(oppivelvollisuusKeskeytettyMääräajaksiPath)
      await mainHeadingEquals(
        "Oppivelvollisuus-keskeytetty-määräajaksi Valpas (181005A1560)",
      )

      // Avaa ov-keskeytysmodaali
      await clickElement("#ovkeskeytys-btn")
      await textEventuallyEquals(
        ".modal__title",
        "Oppivelvollisuuden keskeytys",
      )
      await textEventuallyEquals(
        ".modal__container .heading--secondary",
        "Oppivelvollisuus-keskeytetty-määräajaksi Valpas (181005A1560)",
      )

      // Valitse "Oppivelvollisuus keskeytetään toistaiseksi", säilytä alkupäivänä nykyinen päivä, hyväksy ehto
      await clickElement(
        ".ovkeskeytys__option:nth-child(2) .radiobutton__container",
      )
      await clickElement(
        ".ovkeskeytys__option:nth-child(2) .checkbox__labeltext",
      )
      await clickElement("#ovkeskeytys-submit")

      await oppivelvollisuustiedotEquals(
        oppivelvollisuustiedot({
          opiskelutilanne: "Kyllä",
          oppivelvollisuudenKeskeytykset: [
            `toistaiseksi 11.11.2022 alkaen`,
            `1.9.2021 – 30.9.2021`,
            `1.1.2020 – 30.1.2020`,
          ],
          maksuttomuusoikeus: "31.12.2025 asti",
          oppivelvollisuudenKeskeytysBtn: true,
          kuntailmoitusBtn: true,
          merkitseVapautusBtn: true,
        }),
      )
    })

    it("Oppivelvollisuuden keskeytystä voi muokata vaikka se olisi päättynyt", async () => {
      await loginAs(
        oppivelvollisuusKeskeytettyMääräajaksiPath,
        "valpas-helsinki",
      )

      await resetMockData("2022-11-11")
      await goToLocation(oppivelvollisuusKeskeytettyMääräajaksiPath)
      await mainHeadingEquals(
        "Oppivelvollisuus-keskeytetty-määräajaksi Valpas (181005A1560)",
      )

      await clickElement(".oppijaview__editkeskeytysbtn")
      await textEventuallyEquals(
        ".modal__title",
        "Oppivelvollisuuden keskeytyksen muokkaus",
      )

      await clickElement("#ovkeskeytys-submit-edit")

      await oppivelvollisuustiedotEquals(
        oppivelvollisuustiedot({
          opiskelutilanne: "Kyllä",
          oppivelvollisuus: "17.10.2023 asti",
          oppivelvollisuudenKeskeytykset: [
            `1.9.2021 – 30.9.2021`,
            `1.1.2020 – 30.1.2020`,
          ],
          maksuttomuusoikeus: "31.12.2025 asti",
          oppivelvollisuudenKeskeytysBtn: true,
          kuntailmoitusBtn: true,
          merkitseVapautusBtn: true,
        }),
      )
    })

    it("Näytä väliaikaisesti keskeytynyt opiskeluoikeus", async () => {
      await loginAs(
        opiskeluoikeusKeskeytettyMääräajaksiPath,
        "valpas-jkl-yliopisto-suorittaminen",
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
        }),
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
        }),
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
        }),
      )
    })
  })

  describe("Taiteen perusopetus", () => {
    it("Päättynyt-tila käsitellään oikein", async () => {
      await loginAs(taiteenPerusopetusPäättynytPath, "valpas-pää")
      await resetMockData("2021-08-15")
      await mainHeadingEquals("Taiteilija Petra (010110A955U)")
      await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000180")
    })
  })

  describe("Muuttopäivä Suomeen", () => {
    it("Näytetään jos muuttanut ulkomailta/Ahvenanmaalta Suomeen", async () => {
      await loginAs(maahanmuuttajaPath, "valpas-pää")
      await resetMockData("2021-08-15")
      await mainHeadingEquals("Maahanmuuttaja Masa (010106A431W)")
      await oppivelvollisuustiedotEquals(
        oppivelvollisuustiedot({
          opiskelutilanne: "Ei",
          oppivelvollisuus: "31.12.2023 asti",
          maksuttomuusoikeus: "31.12.2026 asti",
          kotikuntaSuomessaAlkaen: "1.1.2014",
          oppivelvollisuudenKeskeytysBtn: true,
          merkitseVapautusBtn: true,
        }),
      )
    })
  })

  describe("Ammatillisen tutkinnon tarkennukset", () => {
    it("Jos YO tutkinnon vahvistuspäivä on ennen kuin ammatillisen tutkinnon (perus-, ammattitutkinto tai erikoisammattitutkinto) opiskeluoikeus alkaa, päättyy maksuttomuus YO- tutkinnon vahvistuspäivään", async () => {
      await loginAs(ammattitutkintoYoTutkinnonJalkeenPath, "valpas-pää")
      await resetMockData("2021-09-05")
      await mainHeadingEquals(
        "Ammattitutkinto yo-tutkinnon Jälkeen Antti (300805A1918)",
      )
      await oppivelvollisuustiedotEquals(
        oppivelvollisuustiedot({
          opiskelutilanne: "Ei",
          oppivelvollisuus: "5.9.2021 asti",
          maksuttomuusoikeus: "5.9.2021 asti",
          oppivelvollisuudenKeskeytysBtn: true,
          merkitseVapautusBtn: true,
          kuntailmoitusBtn: true,
        }),
      )
    })

    it("Jos ammatillinen tutkinto (perus-, ammattitutkinto tai erikoisammattitutkinto)  on valmistunut (vahvistuspäivän mukaan), ennen kuin lukion opinnot (aine tai koko oppimäärä) alkaa, päättyy maksuttomuus ammatillisen tutkinnon (perus-, ammattitutkinto tai erikoisammattitutkinto) vahvistuspäivään", async () => {
      await loginAs(lukioOpinnotAmmattitutkinnonJalkeenPath, "valpas-pää")
      await resetMockData("2021-09-05")
      await mainHeadingEquals(
        "Lukio-opinnot Ammattitutkinnon Jälkeen Lucia (300805A4409)",
      )
      await oppivelvollisuustiedotEquals(
        oppivelvollisuustiedot({
          opiskelutilanne: "Ei",
          oppivelvollisuus: "5.9.2021 asti",
          maksuttomuusoikeus: "5.9.2021 asti",
          oppivelvollisuudenKeskeytysBtn: true,
          merkitseVapautusBtn: true,
          kuntailmoitusBtn: true,
        }),
      )
    })
  })
})

import { oppijaPath } from "../../src/state/paths"
import { $$, goToLocation } from "../integrationtests-env/browser/core"
import {
  allowNetworkError,
  FORBIDDEN,
} from "../integrationtests-env/browser/fail-on-console"
import { loginAs, resetMockData } from "../integrationtests-env/browser/reset"
import {
  expectEiKuntailmoituksiaNotVisible,
  hautEquals,
  historiaOpintoOikeus,
  ilmoitetutYhteystiedot,
  ilmoitetutYhteystiedotEquals,
  mainHeadingEquals,
  merge,
  opiskeluhistoriaEquals,
  oppivelvollisuustiedot,
  oppivelvollisuustiedotEquals,
  secondaryHeadingEquals,
  turvakieltoVaroitusEquals,
  turvakieltoVaroitusNotVisible,
  virallisetYhteystiedot,
  virallisetYhteystiedotEquals,
} from "./oppija.shared"
import {contentEventuallyEquals, testId} from "../integrationtests-env/browser/content";

const ysiluokkaKeskenKeväälläPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000001",
})
const päällekkäisiäOppivelvollisuuksiaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000003",
})
const ysiluokkaValmisKeväälläPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000011",
})
const lukionAloittanutPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000015",
})
const lukionLokakuussaAloittanutPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000016",
})
const kahdellaOppijaOidillaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000017",
})
const turvakiellollinenOppijaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000024",
})
const epäonninenOppijaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000028",
})
const montaHakuaJoistaYksiPäättynytOppijaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000009",
})
const lukionAineopinnotAloittanutPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000033",
})
const lukioOpiskelijaPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000004",
})
const vsopPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000046",
})

const opiskeluoikeusIntSchoolPerusopetusPath = oppijaPath.href("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000094",
})

describe("Oppijakohtainen näkymä 1/2", () => {
  it("Näyttää oppijan tiedot, johon käyttäjällä on lukuoikeus", async () => {
    await loginAs(ysiluokkaKeskenKeväälläPath, "valpas-jkl-normaali", true)
    await mainHeadingEquals(
      "Oppivelvollinen-ysiluokka-kesken-keväällä-2021 Valpas (221105A3023)"
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000001")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "21.11.2023 asti",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoOikeus({
          otsikko: "Perusopetus 2012 –",
          tila: "Läsnä",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9C",
          alkamispäivä: "15.8.2012",
        }),
        historiaOpintoOikeus({
          otsikko: "Esiopetuksen suoritus 2010 – 2015",
          tila: "Valmistunut",
          toimipiste: "Jyväskylän normaalikoulu",
          alkamispäivä: "13.8.2010",
          päättymispäivä: "1.1.2015",
        })
      )
    )
    await hautEquals(`
      list_alt
      Yhteishaku 2021 Hakenut open_in_new
        Hakukohde
        Valinta
        Pisteet
        Alin pistemäärä
        1. Ressun lukio, Lukio Hylätty 9,00 9,01
        2. Helsingin medialukio, Lukio
        Otettu vastaan
        Hyväksytty 9,00 8,20
        3. Omnia, Leipomoala Peruuntunut – –
        4. Omnia, Puhtaus- ja kiinteistöpalveluala Peruuntunut – –
        5. Varsinais-Suomen kansanopisto, Vapaan sivistystyön koulutus oppivelvollisille 2021-2022 Peruuntunut – –
    `)
  })

  it("Näyttää oppijan, jolla monta hakua, kaikki haut, myös päättyneet", async () => {
    await loginAs(
      montaHakuaJoistaYksiPäättynytOppijaPath,
      "valpas-jkl-normaali"
    )
    await mainHeadingEquals(
      "LuokallejäänytYsiluokkalainen Valpas (020805A5625)"
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000009")
    await hautEquals(`
      list_alt
      Yhteishaku 2021 Hakenut open_in_new
      Hakukohde
      Valinta
      Pisteet
      Alin pistemäärä
      1. Helsingin medialukio, Lukio – – –
      list_alt
      Yhteishaku 2021 Hakenut open_in_new
      Hakukohde
      Valinta
      Pisteet
      Alin pistemäärä
      1. Varsinais-Suomen kansanopisto, Vapaan sivistystyön koulutus oppivelvollisille 2021-2022 – – –
      list_alt
      Päättynyt: Yhteishaku 2019 Hakenut open_in_new
      Hakukohde
      Valinta
      Pisteet
      Alin pistemäärä
      1. Varsinais-Suomen kansanopisto, Vapaan sivistystyön koulutus oppivelvollisille 2019-2020 – – –
    `)
  })

  it("Näyttää oppijan tiedot valmistuneelle ysiluokkalaiselle", async () => {
    await loginAs(ysiluokkaValmisKeväälläPath, "valpas-jkl-normaali")
    await mainHeadingEquals(
      "Ysiluokka-valmis-keväällä-2021 Valpas (190605A006K)"
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000011")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Ei opiskelupaikkaa",
        oppivelvollisuus: "18.6.2023 asti",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
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

  it("Näyttää oppijan tiedot int schoolissa olevalle ysiluokkalaiselle", async () => {
    await loginAs(opiskeluoikeusIntSchoolPerusopetusPath, "valpas-jkl-normaali")
    await mainHeadingEquals(
      "Int-school-9-luokan-jälkeen-lukion-aloittanut Valpas (120505A3434)"
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000094")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "11.5.2023 asti",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoOikeus({
          otsikko: "Lukion oppimäärä 2021 –",
          tila: "Läsnä",
          maksuttomuus: [
            "15.8.2021–16.8.2021 maksuton",
            "17.8.2021–18.8.2021 maksullinen",
            "19.8.2021– maksuton",
          ],
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "AH",
          alkamispäivä: "15.8.2021",
        }),
        historiaOpintoOikeus({
          otsikko: "International school 2004 –",
          tila: "Läsnä",
          maksuttomuus: ["Ei"],
          toimipiste: "International School of Helsinki",
          ryhmä: "9B",
          alkamispäivä: "15.8.2004",
          perusopetusSuoritettu: "30.5.2021",
        })
      )
    )
  })

  it("Näyttää oppijan vsop-tiedon", async () => {
    await loginAs(vsopPath, "valpas-jkl-normaali", true)
    await mainHeadingEquals(
      "Ysiluokka-valmis-keväällä-2021-vsop Valpas (190705A575R)"
    )
    await opiskeluhistoriaEquals(
      historiaOpintoOikeus({
        otsikko: "Perusopetus 2012 – 2021",
        tila: "Valmistunut",
        toimipiste: "Jyväskylän normaalikoulu",
        ryhmä: "9C",
        vuosiluokkiinSitomatonOpetus: true,
        alkamispäivä: "15.8.2012",
        päättymispäivä: "30.5.2021",
      })
    )
  })

  it("Näyttää oppijan muut tiedot vaikka hakukoostekysely epäonnistuu", async () => {
    await loginAs(epäonninenOppijaPath, "valpas-jkl-normaali")
    await mainHeadingEquals("Epäonninen Valpas (301005A336J)")
    await hautEquals("Virhe oppijan hakuhistorian hakemisessa")
  })

  it("Ei näytä oppijan tietoja, johon käyttäjällä ei ole lukuoikeutta", async () => {
    allowNetworkError("/valpas/api/oppija/", FORBIDDEN)
    await loginAs(ysiluokkaKeskenKeväälläPath, "valpas-pelkkä-suorittaminen")

    await mainHeadingEquals("Oppijan tiedot")
    await secondaryHeadingEquals(
      "Oppijaa ei löydy tunnuksella 1.2.246.562.24.00000000001"
    )
    await expectEiKuntailmoituksiaNotVisible()
  })

  it("Ei näytä oppijan tietoja, johon käyttäjällä ei ole lukuoikeutta vaihdetun tarkastelupäivän jälkeen", async () => {
    allowNetworkError("/valpas/api/oppija/", FORBIDDEN)
    await loginAs(ysiluokkaValmisKeväälläPath, "valpas-jkl-normaali-perus")
    await mainHeadingEquals(
      "Ysiluokka-valmis-keväällä-2021 Valpas (190605A006K)"
    )
    await resetMockData("2021-10-05")
    await goToLocation(ysiluokkaValmisKeväälläPath)

    await mainHeadingEquals("Oppijan tiedot")
    await secondaryHeadingEquals(
      "Oppijaa ei löydy tunnuksella 1.2.246.562.24.00000000011"
    )
    await expectEiKuntailmoituksiaNotVisible()
  })

  it("Näyttää oppijalta, jolla on useampia päällekäisiä opiskeluoikeuksia kaikki opiskeluoikeudet", async () => {
    await loginAs(päällekkäisiäOppivelvollisuuksiaPath, "valpas-jkl-normaali")
    await mainHeadingEquals("Päällekkäisiä Oppivelvollisuuksia (060605A083N)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000003")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "5.6.2023 asti",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoOikeus({
          otsikko: "Perusopetus 2012 –",
          tila: "Läsnä",
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "9B",
          alkamispäivä: "15.8.2012",
        }),
        historiaOpintoOikeus({
          otsikko: "Perusopetus 2012 –",
          tila: "Läsnä",
          toimipiste: "Kulosaaren ala-aste",
          ryhmä: "8A",
          alkamispäivä: "15.8.2012",
        })
      )
    )
  })

  it("Näyttää oppijalta, jolla on useampia peräkkäisiä opiskeluoikeuksia kaikki opiskeluoikeudet", async () => {
    await loginAs(lukionAloittanutPath, "valpas-jkl-normaali")
    await mainHeadingEquals("LukionAloittanut Valpas (290405A871A)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000015")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "28.4.2023 asti",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoOikeus({
          otsikko: "Lukion oppimäärä 2021 –",
          tila: "Läsnä",
          maksuttomuus: [
            "15.8.2021–16.8.2021 maksuton",
            "17.8.2021–18.8.2021 maksullinen",
            "19.8.2021– maksuton",
          ],
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "AH",
          alkamispäivä: "15.8.2021",
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

  it("Näyttää oppijalta, jolla opiskeluoikeus alkaa tulevaisuudessa oikeat tiedot", async () => {
    await loginAs(lukionLokakuussaAloittanutPath, "valpas-jkl-normaali")
    await mainHeadingEquals("LukionLokakuussaAloittanut Valpas (180405A819J)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000016")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Ei opiskelupaikkaa",
        oppivelvollisuus: "17.4.2023 asti",
        maksuttomuusoikeus: "31.12.2025 asti",
        kuntailmoitusBtn: true,
      })
    )
    await opiskeluhistoriaEquals(
      merge(
        historiaOpintoOikeus({
          otsikko: "Lukion oppimäärä 2021 –",
          tila: "Opiskeluoikeus alkaa 3.10.2021",
          maksuttomuus: ["3.10.2021– maksuton"],
          toimipiste: "Jyväskylän normaalikoulu",
          ryhmä: "AH",
          alkamispäivä: "3.10.2021",
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

  it("Näyttää oppijan yhteystiedot ilman turvakieltovaroitusta", async () => {
    await loginAs(ysiluokkaKeskenKeväälläPath, "valpas-jkl-normaali")

    const pvm = "9.4.2020"

    await contentEventuallyEquals(testId("kotikunta-yhteystiedot"), "Helsinki")

    await ilmoitetutYhteystiedotEquals(
      ilmoitetutYhteystiedot({
        pvm,
        lähiosoite: "Esimerkkikatu 123",
        postitoimipaikka: "99999 Helsinki",
        matkapuhelin: "0401234567",
        sähköposti:
          "Valpas.Oppivelvollinen-ysiluokka-kesken-keväällä-2021@gmail.com",
        lähde: "Hakulomake – Yhteishaku 2021",
      })
    )

    await virallisetYhteystiedotEquals(
      virallisetYhteystiedot({
        lähiosoite: "Esimerkkitie 10",
        postitoimipaikka: "99999 Helsinki",
        maa: "Costa rica",
        puhelin: "0401122334",
        sähköposti: "valpas@gmail.com",
      })
    )

    // Klikkaukset kääntävät näkyvät ja piilotetut arvot päinvastaiseen tilaan
    const labels = await $$("#yhteystiedot .accordion__trigger")
    await Promise.all(labels.map((label) => label.click()))

    await ilmoitetutYhteystiedotEquals(ilmoitetutYhteystiedot({ pvm }))

    await turvakieltoVaroitusNotVisible()
  })

  it("Näyttää oppijalta uusimman muokatun hakemuksen yhteystiedot", async () => {
    await loginAs(
      montaHakuaJoistaYksiPäättynytOppijaPath,
      "valpas-jkl-normaali"
    )

    await ilmoitetutYhteystiedotEquals(
      ilmoitetutYhteystiedot({
        pvm: "10.4.2020",
        lähiosoite: "Uudempi esimerkkikatu 987",
        postitoimipaikka: "99999 Helsinki",
        matkapuhelin: "0401234567",
        sähköposti: "Valpas.LuokallejäänytYsiluokkalainen@gmail.com",
        lähde: "Hakulomake – Yhteishaku 2021",
      })
    )
  })

  it("Näyttää oppijalta ruotsissa olevan ilmotukselta tulevan osoitteen", async () => {
    await loginAs(päällekkäisiäOppivelvollisuuksiaPath, "valpas-jkl-normaali")

    await ilmoitetutYhteystiedotEquals(
      ilmoitetutYhteystiedot({
        pvm: "9.4.2020",
        lähiosoite: "Kungsgatan 123",
        postitoimipaikka: "99999 STOCKHOLM",
        maa: "Ruotsi",
        matkapuhelin: "0401234567",
        sähköposti: "Oppivelvollisuuksia.Päällekkäisiä@gmail.com",
        lähde: "Hakulomake – Yhteishaku 2021",
      })
    )
  })

  it("Yhteystietoja ei näytetä, jos oppijalla on turvakielto", async () => {
    await loginAs(turvakiellollinenOppijaPath, "valpas-jkl-normaali")

    await turvakieltoVaroitusEquals(`
      warning
      Oppijalla on turvakielto. Yhteystietoja saa käyttää ainoastaan oppivelvollisuuden valvontaan.
    `)

    await contentEventuallyEquals(testId("kotikunta-yhteystiedot"), "Ei tiedossa (kunta)")

    await virallisetYhteystiedotEquals(`
      Viralliset yhteystiedot
      Henkilöllä on turvakielto
    `)
  })

  it("Näyttää varasijan hakutuloksissa", async () => {
    await loginAs(kahdellaOppijaOidillaPath, "valpas-jkl-normaali")
    await mainHeadingEquals("Kahdella-oppija-oidilla Valpas (150205A490C)")
    await hautEquals(`
      list_alt
      Yhteishaku 2021 Hakenut open_in_new
        Hakukohde
        Valinta
        Pisteet
        Alin pistemäärä
        1. Ressun lukio, Lukio 3. varasija 9,00 8,99
    `)
  })

  it("Näyttää harkinnanvaraisen haun", async () => {
    await loginAs(päällekkäisiäOppivelvollisuuksiaPath, "valpas-jkl-normaali")
    await mainHeadingEquals("Päällekkäisiä Oppivelvollisuuksia (060605A083N)")
    await hautEquals(`
      list_alt
      Yhteishaku 2021 Hakenut open_in_new
        Hakukohde
        Valinta
        Pisteet
        Alin pistemäärä
        1. Omnia, Peruuntumisala  Perunut –	–
        2. Helsingin medialukio, Lukio	Hylätty	7,50	8,20
        3. Omnia, Leipomoala1
        Otettu vastaan ehdollisesti
        Hyväksytty	–	–
        4. Omnia, Puhtaus- ja kiinteistöpalveluala	Hyväksytty	–	–
        1) Hakenut harkinnanvaraisesti
    `)
  })

  it("Oppivelvollisuuden suorittamiseen kelpaamattomia opintoja ei näytetä", async () => {
    await loginAs(lukionAineopinnotAloittanutPath, "valpas-jkl-normaali")
    await mainHeadingEquals("LukionAineopinnotAloittanut Valpas (040305A559A)")
    await opiskeluhistoriaEquals(
      historiaOpintoOikeus({
        otsikko: "Perusopetus 2012 – 2021",
        tila: "Valmistunut",
        toimipiste: "Jyväskylän normaalikoulu",
        ryhmä: "9C",
        alkamispäivä: "15.8.2012",
        päättymispäivä: "30.5.2021",
      })
    )
  })

  it("Näyttää detaljisivun maksuttomuuskäyttäjälle lukio-oppijasta", async () => {
    await loginAs(lukioOpiskelijaPath, "valpas-pelkkä-maksuttomuus")

    await mainHeadingEquals("Lukio-opiskelija Valpas (070504A717P)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000004")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "6.5.2022 asti",
        maksuttomuusoikeus: "31.12.2024 asti",
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

  it("Näyttää detaljisivun maksuttomuuskäyttäjälle lukio-oppijasta oppivelvollisuuden päättymisen jälkeenkin", async () => {
    await loginAs(lukioOpiskelijaPath, "valpas-pelkkä-maksuttomuus")

    await resetMockData("2022-08-10")

    await mainHeadingEquals("Lukio-opiskelija Valpas (070504A717P)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000004")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "6.5.2022 asti",
        maksuttomuusoikeus: "31.12.2024 asti",
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

  it("Ei näytä detaljisivua maksuttomuuskäyttäjälle maksuttomuuden päättymisen jälkeen", async () => {
    allowNetworkError("/valpas/api/oppija/", FORBIDDEN)
    await loginAs(lukioOpiskelijaPath, "valpas-pelkkä-maksuttomuus")

    await resetMockData("2025-01-01")
    await goToLocation(lukioOpiskelijaPath)

    await mainHeadingEquals("Oppijan tiedot")
    await secondaryHeadingEquals(
      "Oppijaa ei löydy tunnuksella 1.2.246.562.24.00000000004"
    )
    await expectEiKuntailmoituksiaNotVisible()
  })

  it("Näyttää detaljisivun kuntakäyttäjälle lukio-oppijasta", async () => {
    await loginAs(lukioOpiskelijaPath, "valpas-helsinki")

    await mainHeadingEquals("Lukio-opiskelija Valpas (070504A717P)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000004")
    await oppivelvollisuustiedotEquals(
      oppivelvollisuustiedot({
        opiskelutilanne: "Opiskelemassa",
        oppivelvollisuus: "6.5.2022 asti",
        maksuttomuusoikeus: "31.12.2024 asti",
        kuntailmoitusBtn: true,
        oppivelvollisuudenKeskeytysBtn: true,
        merkitseVapautusBtn: true,
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
})

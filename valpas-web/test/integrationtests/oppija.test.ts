import { createOppijaPath } from "../../src/state/paths"
import {
  contentEventuallyEquals,
  expectElementNotVisible,
  textEventuallyEquals,
} from "../integrationtests-env/browser/content"
import { $$, goToLocation } from "../integrationtests-env/browser/core"
import {
  allowNetworkError,
  FORBIDDEN,
} from "../integrationtests-env/browser/fail-on-console"
import { loginAs, resetMockData } from "../integrationtests-env/browser/reset"

const ysiluokkaKeskenKeväälläPath = createOppijaPath("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000001",
})
const päällekkäisiäOppivelvollisuuksiaPath = createOppijaPath("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000003",
})
const ysiluokkaValmisKeväälläPath = createOppijaPath("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000011",
})
const lukionAloittanutPath = createOppijaPath("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000015",
})
const lukionLokakuussaAloittanutPath = createOppijaPath("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000016",
})
const kahdellaOppijaOidillaPath = createOppijaPath("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000017",
})
const turvakiellollinenOppijaPath = createOppijaPath("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000024",
})
const epäonninenOppijaPath = createOppijaPath("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000028",
})
const montaHakuaJoistaYksiPäättynytOppijaPath = createOppijaPath(
  "/virkailija",
  {
    oppijaOid: "1.2.246.562.24.00000000009",
  }
)
const lukionAineopinnotAloittanutPath = createOppijaPath("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000033",
})
const lukioOpiskelijaPath = createOppijaPath("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000004",
})
const vsopPath = createOppijaPath("/virkailija", {
  oppijaOid: "1.2.246.562.24.00000000046",
})

const mainHeadingEquals = (expected: string) =>
  textEventuallyEquals("h1.heading--primary", expected)
const secondaryHeadingEquals = (expected: string) =>
  textEventuallyEquals(".oppijaview__secondaryheading", expected)
const cardBodyEquals = (id: string) => (expected: string) =>
  contentEventuallyEquals(`#${id} .card__body`, expected)
const oppivelvollisuustiedotEquals = cardBodyEquals("oppivelvollisuustiedot")
const opiskeluhistoriaEquals = cardBodyEquals("opiskeluhistoria")
const hautEquals = cardBodyEquals("haut")
const ilmoitetutYhteystiedotEquals = (expected: string) =>
  contentEventuallyEquals("#ilmoitetut-yhteystiedot", expected)
const virallisetYhteystiedotEquals = (expected: string) =>
  contentEventuallyEquals("#viralliset-yhteystiedot", expected)
const turvakieltoVaroitusEquals = (expected: string) =>
  contentEventuallyEquals("#turvakielto-varoitus", expected)
const turvakieltoVaroitusNotVisible = () =>
  expectElementNotVisible("#turvakielto-varoitus")
const expectEiKuntailmoituksiaNotVisible = () =>
  expectElementNotVisible(".oppijaview__eiilmoituksia")

describe("Oppijakohtainen näkymä", () => {
  it("Näyttää oppijan tiedot, johon käyttäjällä on lukuoikeus (flaky)", async () => {
    await loginAs(ysiluokkaKeskenKeväälläPath, "valpas-jkl-normaali", true)
    await mainHeadingEquals(
      "Oppivelvollinen-ysiluokka-kesken-keväällä-2021 Valpas (221105A3023)"
    )
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000001")
    await oppivelvollisuustiedotEquals(`
      Opiskelutilanne:	Opiskelemassa
      Oppivelvollisuus:	22.11.2023 asti
      Oikeus opintojen maksuttomuuteen: 31.12.2025 asti
    `)
    await opiskeluhistoriaEquals(`
      school
      Perusopetus 2012 –
      Jyväskylän normaalikoulu
      Ryhmä: 9C
      Tila: Opiskeluoikeus voimassa
    `)
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
    await oppivelvollisuustiedotEquals(`
      Opiskelutilanne:	Ei opiskelupaikkaa
      Oppivelvollisuus:	19.6.2023 asti
      Oikeus opintojen maksuttomuuteen: 31.12.2025 asti
    `)
    await opiskeluhistoriaEquals(`
      school
      Perusopetus 2012 – 2021
      Jyväskylän normaalikoulu
      Ryhmä: 9C
      Tila: Valmistunut 30.5.2021
    `)
  })

  it("Näyttää oppijan vsop-tiedon", async () => {
    await loginAs(vsopPath, "valpas-jkl-normaali", true)
    await mainHeadingEquals(
      "Ysiluokka-valmis-keväällä-2021-vsop Valpas (190705A575R)"
    )
    await opiskeluhistoriaEquals(`
      school
      Perusopetus 2012 – 2021
      Jyväskylän normaalikoulu
      Ryhmä: 9C
      Tila: Valmistunut 30.5.2021
      Muuta: Vuosiluokkiin sitomaton opetus
    `)
  })

  it("Näyttää oppijan muut tiedot vaikka hakukoostekysely epäonnistuu", async () => {
    await loginAs(epäonninenOppijaPath, "valpas-jkl-normaali")
    await mainHeadingEquals("Epäonninen Valpas (301005A336J)")
    await hautEquals("Virhe oppijan hakuhistorian hakemisessa")
  })

  it("Ei näytä oppijan tietoja, johon käyttäjällä ei ole lukuoikeutta", async () => {
    allowNetworkError("/valpas/api/oppija/", FORBIDDEN)
    await loginAs(ysiluokkaKeskenKeväälläPath, "valpas-pelkkä-suorittaminen")

    await textEventuallyEquals(
      ".ohjeteksti",
      "Olet onnistuneesti kirjautunut Valpas-järjestelmään seuraavilla käyttöoikeuksilla",
      5000
    )
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
    await oppivelvollisuustiedotEquals(`
      Opiskelutilanne:	Opiskelemassa
      Oppivelvollisuus:	6.6.2023 asti
      Oikeus opintojen maksuttomuuteen: 31.12.2025 asti
    `)
    await opiskeluhistoriaEquals(`
      school
      Perusopetus 2012 –
      Jyväskylän normaalikoulu
      Ryhmä: 9B
      Tila: Opiskeluoikeus voimassa
      school
      Perusopetus 2012 –
      Kulosaaren ala-aste
      Ryhmä: 8A
      Tila: Opiskeluoikeus voimassa
    `)
  })

  it("Näyttää oppijalta, jolla on useampia peräkkäisiä opiskeluoikeuksia kaikki opiskeluoikeudet", async () => {
    await loginAs(lukionAloittanutPath, "valpas-jkl-normaali")
    await mainHeadingEquals("LukionAloittanut Valpas (290405A871A)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000015")
    await oppivelvollisuustiedotEquals(`
      Opiskelutilanne:	Opiskelemassa
      Oppivelvollisuus:	29.4.2023 asti
      Oikeus opintojen maksuttomuuteen: 31.12.2025 asti
    `)
    await opiskeluhistoriaEquals(`
      school
      Lukiokoulutus 2021 –
      Jyväskylän normaalikoulu
      Ryhmä: AH
      Tila: Opiskeluoikeus voimassa
      school
      Perusopetus 2012 – 2021
      Jyväskylän normaalikoulu
      Ryhmä: 9C
      Tila: Valmistunut 30.5.2021
    `)
  })

  it("Näyttää oppijalta, jolla opiskeluoikeus alkaa tulevaisuudessa oikeat tiedot", async () => {
    await loginAs(lukionLokakuussaAloittanutPath, "valpas-jkl-normaali")
    await mainHeadingEquals("LukionLokakuussaAloittanut Valpas (180405A819J)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000016")
    await oppivelvollisuustiedotEquals(`
      Opiskelutilanne:	Ei opiskelupaikkaa
      Oppivelvollisuus:	18.4.2023 asti
      Oikeus opintojen maksuttomuuteen: 31.12.2025 asti
    `)
    await opiskeluhistoriaEquals(`
      school
      Lukiokoulutus 2021 –
      Jyväskylän normaalikoulu
      Ryhmä: AH
      Tila: Opiskeluoikeus alkaa 3.10.2021
      school
      Perusopetus 2012 – 2021
      Jyväskylän normaalikoulu
      Ryhmä: 9C
      Tila: Valmistunut 30.5.2021
    `)
  })

  it("Näyttää oppijan yhteystiedot ilman turvakieltovaroitusta", async () => {
    await loginAs(ysiluokkaKeskenKeväälläPath, "valpas-jkl-normaali")

    await ilmoitetutYhteystiedotEquals(`
      Ilmoitetut yhteystiedot
      keyboard_arrow_downYhteystiedot – 9.4.2020
      Lähiosoite:	Esimerkkikatu 123
      Postitoimipaikka:  00000 Helsinki
      Matkapuhelin:	0401234567
      Sähköposti:	Valpas.Oppivelvollinen-ysiluokka-kesken-keväällä-2021@gmail.com
      Lähde: Hakulomake – Yhteishaku 2021
    `)

    await virallisetYhteystiedotEquals(`
      Viralliset yhteystiedot
      keyboard_arrow_downVTJ: Kotiosoite
      Lähiosoite:	Esimerkkitie 10
      Postitoimipaikka:	00000 Helsinki
      Maa: Costa rica
      Puhelin:	0401122334
      Sähköposti:	valpas@gmail.com
    `)

    // Klikkaukset kääntävät näkyvät ja piilotetut arvot päinvastaiseen tilaan
    const labels = await $$("#yhteystiedot .accordion__label")
    await Promise.all(labels.map((label) => label.click()))

    await ilmoitetutYhteystiedotEquals(`
      Ilmoitetut yhteystiedot
      keyboard_arrow_rightYhteystiedot – 9.4.2020
    `)

    await turvakieltoVaroitusNotVisible()
  })

  it("Näyttää oppijalta uusimman muokatun hakemuksen yhteystiedot", async () => {
    await loginAs(
      montaHakuaJoistaYksiPäättynytOppijaPath,
      "valpas-jkl-normaali"
    )

    await ilmoitetutYhteystiedotEquals(`
      Ilmoitetut yhteystiedot
      keyboard_arrow_downYhteystiedot – 10.4.2020
      Lähiosoite:	Uudempi esimerkkikatu 987
      Postitoimipaikka:  00000 Helsinki
      Matkapuhelin:	0401234567
      Sähköposti:	Valpas.LuokallejäänytYsiluokkalainen@gmail.com
      Lähde: Hakulomake – Yhteishaku 2021
    `)
  })

  it("Näyttää oppijalta ruotsissa olevan ilmotukselta tulevan osoitteen", async () => {
    await loginAs(päällekkäisiäOppivelvollisuuksiaPath, "valpas-jkl-normaali")

    await ilmoitetutYhteystiedotEquals(`
      Ilmoitetut yhteystiedot
      keyboard_arrow_downYhteystiedot – 9.4.2020
      Lähiosoite:	Kungsgatan 123
      Postitoimipaikka:  00000 STOCKHOLM
      Maa: Ruotsi
      Matkapuhelin:	0401234567
      Sähköposti:	Oppivelvollisuuksia.Päällekkäisiä@gmail.com
      Lähde: Hakulomake – Yhteishaku 2021
    `)
  })

  it("Yhteystietoja ei näytetä, jos oppijalla on turvakielto", async () => {
    await loginAs(turvakiellollinenOppijaPath, "valpas-jkl-normaali")

    await turvakieltoVaroitusEquals(`
      warning
      Oppijalla on turvakielto. Yhteystietoja saa käyttää ainoastaan oppivelvollisuuden valvontaan.
    `)

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
        1. Helsingin medialukio, Lukio	Hylätty	7,50	8,20
        2. Omnia, Leipomoala1
        Otettu vastaan ehdollisesti
        Hyväksytty	–	–
        3. Omnia, Puhtaus- ja kiinteistöpalveluala	Hyväksytty	–	–
        1) Hakenut harkinnanvaraisesti
    `)
  })

  it("Oppivelvollisuuden suorittamiseen kelpaamattomia opintoja ei näytetä", async () => {
    await loginAs(lukionAineopinnotAloittanutPath, "valpas-jkl-normaali")
    await mainHeadingEquals("LukionAineopinnotAloittanut Valpas (040305A559A)")
    await opiskeluhistoriaEquals(`
      school
      Perusopetus 2012 – 2021
      Jyväskylän normaalikoulu
      Ryhmä: 9C
      Tila: Valmistunut 30.5.2021
    `)
  })

  it("Näyttää detaljisivun maksuttomuuskäyttäjälle lukio-oppijasta", async () => {
    await loginAs(lukioOpiskelijaPath, "valpas-pelkkä-maksuttomuus")

    await mainHeadingEquals("Lukio-opiskelija Valpas (070504A717P)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000004")
    await oppivelvollisuustiedotEquals(`
      Opiskelutilanne:	Opiskelemassa
      Oppivelvollisuus:	7.5.2022 asti
      Oikeus opintojen maksuttomuuteen: 31.12.2024 asti
    `)
    await opiskeluhistoriaEquals(`
      school
      Lukiokoulutus 2019 –
      Jyväskylän normaalikoulu
      Ryhmä: AH
      Tila: Opiskeluoikeus voimassa
    `)
  })

  it("Näyttää detaljisivun maksuttomuuskäyttäjälle lukio-oppijasta oppivelvollisuuden päättymisen jälkeenkin", async () => {
    await loginAs(lukioOpiskelijaPath, "valpas-pelkkä-maksuttomuus")

    await resetMockData("2022-08-10")

    await mainHeadingEquals("Lukio-opiskelija Valpas (070504A717P)")
    await secondaryHeadingEquals("Oppija 1.2.246.562.24.00000000004")
    await oppivelvollisuustiedotEquals(`
      Opiskelutilanne:	Opiskelemassa
      Oppivelvollisuus:	7.5.2022 asti
      Oikeus opintojen maksuttomuuteen: 31.12.2024 asti
    `)
    await opiskeluhistoriaEquals(`
      school
      Lukiokoulutus 2019 –
      Jyväskylän normaalikoulu
      Ryhmä: AH
      Tila: Opiskeluoikeus voimassa
    `)
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
    await oppivelvollisuustiedotEquals(`
      Opiskelutilanne:	Opiskelemassa
      Oppivelvollisuus:	7.5.2022 asti
      Oikeus opintojen maksuttomuuteen: 31.12.2024 asti
    `)
    await opiskeluhistoriaEquals(`
      school
      Lukiokoulutus 2019 –
      Jyväskylän normaalikoulu
      Ryhmä: AH
      Tila: Opiskeluoikeus voimassa
    `)
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
})
